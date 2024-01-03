package example.routes

import example.services.UserService
import zio.*
import zio.json.*
import zio.http.*
import example.models.*
import java.util.UUID
import java.io.IOException
import example.servers.*
final case class UserRoutes(userService: UserService):
  val routes = Routes(
    Method.GET / "users" / zio.http.uuid("id") ->
      handler { (id: UUID, _: Request) =>
        userService.getUser(UserId(id)).map(user => Response.json(user.toJson))
      },
    Method.POST / "users" / string("name") ->
      handler { (name: String, _: Request) =>
        userService.createUser(name).map(_.toJson).map(Response.json(_))
      },
    Method.DELETE / "users" / zio.http.uuid("id") ->
      handler { (id: UUID, _: Request) =>
        userService.deleteUser(UserId(id)).map(_ => Response.ok)
      },
    Method.GET / "users" -> handler {
      userService
        .getAll()
        .map(_.toJson)
        .map(Response.json(_))
    },
    Method.PUT / "users" / zio.http.uuid("id") -> handler {
      (id: UUID, req: Request) =>
        {
          for
            updateRequest <- ServerUtils.parseRequestBody[UpdateUser](req)
            _ <- userService.updateUser(UserId(id), updateRequest)
          yield Response.ok
        }
    }
  ).handleError({
    case e: IOException => Response.error(Status.InternalServerError)
    case _              => Response.error(Status.BadRequest)
  })

object UserRoutes:
  val live = ZLayer.fromFunction(UserRoutes.apply)
