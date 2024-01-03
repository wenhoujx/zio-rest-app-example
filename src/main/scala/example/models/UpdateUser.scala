package example.models

import zio.json.*

final case class UpdateUser(
    name: Option[String]
)

object UpdateUser:
  given codec: JsonCodec[UpdateUser] = DeriveJsonCodec.gen[UpdateUser]
