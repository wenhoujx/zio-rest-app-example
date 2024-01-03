package example.servers

import zio.*
import zio.json.*
import zio.http.*
import example.errors.*

object ServerUtils:
  def parseRequestBody[A: JsonDecoder](req: Request): IO[AppError, A] =
    for
      body <- req.body.asString
        .mapError(_.getMessage)
        .mapError(AppError.InvalidBodyError.apply)
      parsed <- ZIO
        .from(body.fromJson[A])
        .mapError(AppError.InvalidBodyError.apply)
    yield parsed
