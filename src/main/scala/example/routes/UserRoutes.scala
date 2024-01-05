package example.routes

import example.services.UserService
import zio.*
import zio.json.*
import zio.http.*
import example.models.*
import example.errors.*
import java.util.UUID
import java.io.IOException
import example.servers.*
import example.services.MarriageService

final case class UserRoutes(
    userService: UserService,
    marriageService: MarriageService
):

  val routes = Routes(
    Method.GET / "users" / zio.http.uuid("id") ->
      handler { (id: UUID, _: Request) =>
        service
          .getUser(UserId(id))
          .map(_.toJson)
          .map(Response.json(_))
      },
    Method.POST / "users" / string("name") ->
      handler { (name: String, _: Request) =>
        service
          .createUser(name)
          .map(_.toJson)
          .map(Response.json(_))
      },
    Method.DELETE / "users" / zio.http.uuid("id") ->
      handler { (id: UUID, _: Request) =>
        service.deleteUser(UserId(id)).map(_ => Response.ok)
      },
    Method.GET / "users" -> handler {
      service.getAll().map(allUsers => Response.json(allUsers.toJson))
    },
    Method.PUT / "users" / zio.http.uuid("id") -> handler {
      (id: UUID, req: Request) =>
        {
          for
            updateRequest <- ServerUtils.parseRequestBody[UpdateUser](req)
            res <- service
              .updateUser(UserId(id), updateRequest)
              .map(_ => Response.ok)
          yield res
        }
    }
  ).handleError({
    case e: IOException => Response.error(Status.InternalServerError)
    case _              => Response.error(Status.BadRequest)
  })

  // we need a zio test friendly service that merge UserService and MarriageService,
  // and it's this val implementation
  val service: UserService = new:
    override def createUser(name: String): Task[User] =
      userService.createUser(name)
    override def deleteUser(id: UserId): Task[Unit] =
      for
        status <- marriageService.marriageStatus(id)
        _ <-
          if (status.isDefined)
            ZIO.fail(AppError.CannotDeleteMarriedUserError(id))
          else userService.deleteUser(id)
      yield ()
    override def exists(id: UserId): Task[Boolean] = userService.exists(id)
    override def getAll(): Task[List[User]] = userService.getAll()
    override def getUser(id: UserId): Task[Option[User]] =
      userService.getUser(id)
    override def updateUser(
        id: UserId,
        updateUser: UpdateUser
    ): Task[Option[User]] = userService.updateUser(id, updateUser)

object UserRoutes:
  val live = ZLayer.fromFunction(UserRoutes.apply)
