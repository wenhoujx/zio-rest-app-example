package example.servers

import zio.*
import zio.json.*
import zio.http.*
import example.errors.*

object ServerUtils:
  def parseRequestBody[A: JsonDecoder](req: Request): IO[AppError, A] =
    val body =
      req.body.asString.mapError(t => AppError.InvalidBodyError(t.getMessage))
    body.flatMap(b =>
      ZIO.from(b.fromJson[A]).mapError(AppError.InvalidBodyError.apply)
    )
