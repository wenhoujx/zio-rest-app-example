package example.servers

import example.models.*
import example.services.UserService

import zio.*
import zio.UIO

final case class InRamUserServer(mapRef: Ref[Map[UserId, User]])
    extends UserService {
  override def exists(id: UserId): Task[Boolean] =
    for map <- mapRef.get
    yield map.contains(id)

  override def deleteUser(id: UserId): Task[Unit] =
    for
      map <- mapRef.get
      _ <- mapRef.set(map.removed(id))
    yield ()

  override def getUser(id: UserId): Task[Option[User]] =
    for map <- mapRef.get
    yield map.get(id)

  override def updateUser(
      id: UserId,
      updateUser: UpdateUser
  ): Task[Option[User]] =
    for
      map <- mapRef.get
      oldUser <- ZIO.attempt(map.get(id))
      newUser <- ZIO.succeed(
        oldUser.zip(updateUser.name).map((oldU, newName) => User(id, newName))
      )
      _ <- newUser.fold(ZIO.succeed(()))(nu =>
        mapRef.set(map ++ Map(nu.id -> nu))
      )
    yield newUser

  override def createUser(name: String): Task[User] =
    for
      user <- User.make(name)
      map <- mapRef.get
      _ <- mapRef.set(map ++ Map(user.id -> user))
    yield user

  override def getAll(): Task[List[User]] =
    for map <- mapRef.get
    yield map.values.toList
}

object InRamUserServer {
  lazy val live = ZLayer.fromFunction(InRamUserServer.apply)
  lazy val userExistsLive =
    ZLayer.fromFunction((map: Ref[Map[UserId, User]]) => { (id: UserId) =>
      InRamUserServer(map).getUser(id).map(userOption => !userOption.isEmpty)
    })
}
