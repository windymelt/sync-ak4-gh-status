package com.github.windymelt.syncak4ghstatus

import caliban.client.SelectionBuilder
import client.Client
import com.amazonaws.services.lambda.runtime.Context
import org.apache.logging.log4j.LogManager

import java.io.InputStream
import java.io.OutputStream
import scala.io.Source

case class Input(status: String)

enum Status:
  case In
  case Out

object Status {
  def unapply(s: String): Option[Status] = s match
    case "in"  => Some(Status.In)
    case "out" => Some(Status.Out)
    case _     => None
}

object Main {
  import io.circe._, io.circe.generic.auto._, io.circe.parser._

  val logger = LogManager.getLogger(this.getClass())

  def handler(in: InputStream, out: OutputStream, ctx: Context): Unit = {
    logger.info("lambda handler started")
    val jsonString = Source.fromInputStream(in).mkString
    logger.info(s"got json: $jsonString")
    val payload =
      parse(jsonString)
        .map(_.hcursor.downField("responsePayload").`as`[Input])
        .flatten
    logger.info(s"got payload: $payload")
    payload match
      case Left(err) =>
        logger.error(err.getMessage()) // LoggerはデフォルトではErrorレベルに設定されている?
      case Right(input) =>
        input.status match
          case Status(s) =>
            out.write(s"status is $s".getBytes())
            setGhStatus(s)
            logger.info("lambda handler closing")
          case s =>
            logger.error(s"invalid status: $s")
    in.close()
    out.flush()
    out.close()
  }

  @main def main(args: String*): Unit = setGhStatus(Status.Out)
}

def setGhStatus(status: Status): Unit = {
  import sttp.client3._

  val token = sys.env.get("GH_TOKEN").getOrElse("")
  val isBusy = status match {
    case Status.In  => false
    case Status.Out => true
  }
  val mutation =
    Client.Mutation.changeUserStatus(
      Client.ChangeUserStatusInput(
        None,
        None,
        None,
        Some(isBusy),
        Some("busy"),
        None
      )
    )(
      Client.ChangeUserStatusPayload.status(
        Client.UserStatus.indicatesLimitedAvailability
      )
    )

  val serverUrl = uri"https://api.github.com/graphql"

  implicit val client = SimpleHttpClient()
  val req = mutation.toRequest(
    uri = serverUrl,
    useVariables = false,
    queryName = None,
    dropNullInputValues = false
  )
  val res = client.send(req.header("Authorization", s"Bearer $token"))
  println(res.body)
}
