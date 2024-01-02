# Scala 3 + ZIO 2 rest app example

This is a simple ZIO 2 scala 3 rest app.

## libraries

- `zio 2`
- `zio-json` for json ser/de
- `zio-http` for http server
- `zio-logging` for logging

## run server

This server use in ram hashmap as DB, no docker needed.

Server will start on `localhost:8080`, go to [health endpoint](http://localhost:8080/health) in your browser and you should see `all good`.

```bash
sbt run
```

## run tests

```bash
sbt test
```

## REST curl test

You can find the http request examples in the [test.http](./http/test.http) file.

## TODOs

- [ ] Add tests
- [ ] add db connections
- [x] add instruction how to run this app locally
- [x] add curl examples
- [x] add logging
- [ ] add zio metrics
- [ ] add dockerfile
