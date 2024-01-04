package example.routes

import example.services.*
import zio.*
import zio.http.*
import zio.json.*
import example.servers.{ServerUtils => Utils}
import example.models.MarriageRequest.MarryRequest
import example.errors.AppError
import example.models.MarriageRequest.DivorceRequest
import java.util.UUID
import example.models.UserId
import example.models.Marriage

final case class MarriageRoutes(
    userService: UserService,
    marriageService: MarriageService
):
  val routes = Routes(
    Method.PUT / "marriages" / "marry" -> handler((req: Request) => {
      for
        marryRequest <- Utils.parseRequestBody[MarryRequest](req)
        m <- service.marry(marryRequest.userA, marryRequest.userB)
      yield Response.json(m.toJson)
    }),
    Method.PUT / "marriages" / "divorce" -> handler((req: Request) => {
      for
        divorceRequest <- Utils.parseRequestBody[DivorceRequest](req)
        d <- service
          .divorce(divorceRequest.userA, divorceRequest.userB)
          .map(_ => Response.ok)
      yield Response.ok
    }),
    Method.GET / "marriages" / "status" / zio.http.uuid("uId") -> handler {
      (uId: UUID, req: Request) =>
        service
          .marriageStatus(UserId(uId))
          .map(status => status.fold("{}")(_.toJson))
          .map(Response.json(_))
    }
  ).handleError({ case _ => Response.badRequest })

  val service: MarriageService = new:
    override def marry(userA: UserId, userB: UserId): Task[Marriage] =
      for
        aExists <- userService.exists(userA)
        bExists <- userService.exists(userB)
        res <-
          if (aExists && bExists)
            marriageService.marry(userA, userB)
          else if (!aExists)
            ZIO.fail(AppError.MissingUserError(userA))
          else ZIO.fail(AppError.MissingUserError(userB))
      yield res

    override def divorce(userA: UserId, userB: UserId): Task[Unit] =
      for
        aExists <- userService.exists(userA)
        bExists <- userService.exists(userB)
        res <-
          if (aExists && bExists)
            marriageService.divorce(userA, userB)
          else if (!aExists)
            ZIO.fail(AppError.MissingUserError(userA))
          else ZIO.fail(AppError.MissingUserError(userB))
      yield ()
    override def marriageStatus(userId: UserId): Task[Option[Marriage]] =
      for
        userExists <- userService.exists(userId)
        status <-
          if (userExists)
            marriageService.marriageStatus(userId)
          else
            ZIO.fail(AppError.MissingUserError(userId))
      yield status

object MarriageRoutes:
  lazy val live
      : ZLayer[UserService & MarriageService, Nothing, MarriageRoutes] =
    ZLayer.fromFunction(MarriageRoutes.apply)
