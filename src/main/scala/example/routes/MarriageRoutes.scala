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

final case class MarriageRoutes(
    userService: UserService,
    marriageService: MarriageService
):
  val routes = Routes(
    Method.PUT / "marriages" / "marry" -> handler((req: Request) => {
      for
        marryRequest <- Utils.parseRequestBody[MarryRequest](req)
        aExists <- userService.exists(marryRequest.userA)
        bExists <- userService.exists(marryRequest.userB)

        marriage <-
          if (aExists && bExists)
            marriageService.marry(marryRequest.userA, marryRequest.userB)
          else if (aExists)
            ZIO.fail(AppError.MissingUserError(marryRequest.userB))
          else
            ZIO.fail(AppError.MissingUserError(marryRequest.userA))
      yield Response.json(marriage.toJson)
    }),
    Method.PUT / "marriages" / "divorce" -> handler((req: Request) => {
      for
        divorceRequest <- Utils.parseRequestBody[DivorceRequest](req)
        aExists <- userService.exists(divorceRequest.userA)
        bExists <- userService.exists(divorceRequest.userB)

        divorce <-
          if (aExists && bExists)
            marriageService.divorce(divorceRequest.userA, divorceRequest.userB)
          else if (aExists)
            ZIO.fail(AppError.MissingUserError(divorceRequest.userB))
          else
            ZIO.fail(AppError.MissingUserError(divorceRequest.userA))
      yield Response.ok
    }),
    Method.GET / "marriages" / "status" / zio.http.uuid("uId") -> handler {
      (uId: UUID, req: Request) =>
        {
          val userId = UserId(uId)
          for
            userExists <- userService.exists(userId)
            status <-
              if (userExists)
                marriageService.marriageStatus(userId)
              else
                ZIO.fail(AppError.MissingUserError(userId))
          yield Response.json(status.toJson)
        }
    }
  )

object MarriageRoutes:
  lazy val live = ZLayer.fromFunction(MarriageRoutes.apply)
