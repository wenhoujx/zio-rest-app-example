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
    UserId.random.map(User(_, name))
  given codec: JsonCodec[User] = DeriveJsonCodec.gen[User]
