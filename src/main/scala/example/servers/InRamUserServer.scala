package example.servers

import example.models.*
import example.services.UserService

import scala.collection.concurrent.Map
import zio.*
import zio.UIO

final case class InRamUserServer(map: Map[UserId, User]) extends UserService {
  override def deleteUser(id: UserId): Task[Unit] =
    ZIO.attempt(map.remove(id))

  override def getUser(id: UserId): Task[Option[User]] =
    ZIO.attempt(map.get(id))

  override def updateUser(
      id: UserId,
      updateUser: UpdateUser
  ): Task[Option[User]] =
    val newUser: Option[User] = map
      .get(id)
      .zip(updateUser.name)
      .map((oldU, newName) => {
        User(id, newName)
      })
    ZIO.attempt({
      newUser.foreach(map.put(id, _))
      newUser
    })

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
  lazy val userExistsLive =
    ZLayer.fromFunction((map: Map[UserId, User]) => { (id: UserId) =>
      InRamUserServer(map).getUser(id).map(userOption => !userOption.isEmpty)
    })
}
