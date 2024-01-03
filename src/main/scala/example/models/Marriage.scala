package example.models

import zio.*
import zio.json.*
import example.errors.*

final case class Marriage private (userA: UserId, userB: UserId)

object Marriage:
  def make(ua: UserId, ub: UserId): Task[Marriage] =
    if (ua == ub) then ZIO.fail(AppError.CannotSelfMarryError)
    else ZIO.succeed(Marriage(ua, ub))
    
  given codec: JsonCodec[Marriage] = DeriveJsonCodec.gen[Marriage]
