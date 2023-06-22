val scala3Version = "3.3.0"
val http4sVersion = "0.23.19"
val circeVersion = "0.14.1"

enablePlugins(CalibanPlugin)

addCommandAlias(
  "generateSchema",
  "calibanGenClient project/schema.graphql src/main/scala/client/Client.scala"
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "sync-ak4-gh-status",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
      "org.apache.logging.log4j" % "log4j-api" % "2.20.0",
      "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.20.0",
      "org.slf4j" % "slf4j-api" % "2.0.7",
      "io.microlam" % "slf4j-simple-lambda" % "2.0.3_1",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.4.0",
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "com.github.ghostdogpr" %% "caliban-client" % "2.2.1"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )

ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case x                                   => MergeStrategy.first
}
