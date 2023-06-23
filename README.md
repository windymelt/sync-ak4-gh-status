## Sync ak4 status to GitHub Status

### Usage

Trigger this lambda by another lambda.

Trigger lambda should output either of one:

```json
{"status":"in"}
```

```json
{"status":"out"}
```

`in` makes GitHub status to *not busy*(nominal). `out` makes GitHub status to *busy*.
