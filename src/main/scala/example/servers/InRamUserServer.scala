package example.servers

import example.models.*
import example.services.UserService

import scala.collection.concurrent.Map
import zio.*

final case class InRamUserServer(map: Map[UserId, User]) extends UserService {
  override def deleteUser(id: UserId): Task[Unit] =
    ZIO.attempt(map.remove(id))

  override def getUser(id: UserId): Task[Option[User]] =
    ZIO.attempt(map.get(id))

  override def createUser(name: String): Task[User] =
    for
      user <- User.make(name)
      _ <- ZIO.attempt(map.put(user.id, user))
    yield user

  override def getAll(): Task[List[User]] =
    ZIO.attempt(map.values.toList)
}

object InRamUserServer {
  lazy val live = ZLayer.fromFunction(InRamUserServer.apply)
}
