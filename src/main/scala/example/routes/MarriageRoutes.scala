package example.routes

import example.models.*
import example.services.*
import zio.*
import zio.http.*
import example.servers.{ServerUtils => Utils}
import example.models.MarriageRequest.MarryRequest

final case class MarriageRoutes(marriageService: MarriageService):
  val routes = Routes(
    Method.PUT / "marriages" / "marry" -> handler((req: Request) => {
      for
        marryRequest <- Utils.parseRequestBody[MarryRequest](req)
        marriage <- marriageService
          .marry(marryRequest.userA, marryRequest.userB)
      yield Response.json(???)
    })
  )

object MarriageRoutes:
  lazy val live = ZLayer.fromFunction(MarriageRoutes.apply)
