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
          .map(_.toJson)
          .map(Response.json(_))
    }
  ).handleError({ case _ => Response.badRequest })

  val service: MarriageService = new:
    override def marry(userA: UserId, userB: UserId): Task[Marriage] =
      userService.exists(userA).zip(userService.exists(userB)).flatMap {
        case (false, _)   => ZIO.fail(AppError.MissingUserError(userA))
        case (_, false)   => ZIO.fail(AppError.MissingUserError(userB))
        case (true, true) => marriageService.marry(userA, userB)
      }

    override def divorce(userA: UserId, userB: UserId): Task[Unit] =
      userService.exists(userA).zip(userService.exists(userB)).flatMap {
        case (false, _)   => ZIO.fail(AppError.MissingUserError(userA))
        case (_, false)   => ZIO.fail(AppError.MissingUserError(userB))
        case (true, true) => marriageService.divorce(userA, userB)
      }

    override def marriageStatus(userId: UserId): Task[Option[Marriage]] =
      userService
        .exists(userId)
        .flatMap(exist =>
          if (exist)
            marriageService.marriageStatus(userId)
          else
            ZIO.fail(AppError.MissingUserError(userId))
        )

object MarriageRoutes:
  lazy val live
      : ZLayer[UserService & MarriageService, Nothing, MarriageRoutes] =
    ZLayer.fromFunction(MarriageRoutes.apply)
