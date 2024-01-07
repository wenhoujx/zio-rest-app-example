package example.servers

import example.models.*
import example.services.UserService

import zio.*
import zio.UIO
import example.errors.*

final case class InRamUserServer(mapRef: Ref[Map[UserId, User]])
    extends UserService {
  override def exists(id: UserId): Task[Boolean] =
    mapRef.get.map(_.get(id).isDefined)

  override def deleteUser(id: UserId): Task[Unit] =
    mapRef.update(m => m - id)

  override def getUser(id: UserId): Task[Option[User]] =
    mapRef.get.map(_.get(id))

  override def updateUser(
      id: UserId,
      updateUser: UpdateUser
  ): Task[Option[User]] =
    mapRef.get
      .map(_.get(id))
      .flatMap(
        _.zip(updateUser.name).fold(ZIO.fail(AppError.MissingUserError(id)))(
          (oldU, newName) =>
            mapRef
              .updateAndGet(m => m + ((oldU.id, User(oldU.id, newName))))
              .map(_.get(oldU.id))
        )
      )

  override def createUser(name: String): Task[User] =
    User
      .make(name)
      .flatMap(u =>
        mapRef
          .updateAndGet(m => m + ((u.id, u)))
          .map(_.get(u.id).get)
      )

  override def getAll(): Task[List[User]] =
    mapRef.get.map(_.values.toList)
}

object InRamUserServer {
  lazy val live: ZLayer[Ref[Map[UserId, User]], Nothing, UserService] =
    ZLayer.fromFunction(InRamUserServer.apply)
}
