package example.models

import zio.*
import zio.json.*

final case class User(
    id: UserId,
    name: String
)

// companion object
object User:
  def make(name: String): UIO[User] =
    ???
  given codec: JsonCodec[User] = DeriveJsonCodec.gen[User]
  
