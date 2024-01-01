package example.models

import zio.*
import zio.json.*
import java.util.UUID

final case class UserId(id: UUID)

object UserId {
  def random: UIO[UserId] = Random.nextUUID.map(UserId(_))

  def fromString(id: String): Task[UserId] =
    ZIO.attempt {
      UserId(UUID.fromString(id))
    }

  given codec: JsonCodec[UserId] =
    JsonCodec[UUID].transform(UserId(_), _.id)
}
