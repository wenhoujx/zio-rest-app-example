package example.routes

import zio.ZLayer
import zio.http.*

object HealthRoutes:
  val live = ZLayer.succeed(HealthRoutes)

  val routes = Routes(
    Method.GET / "health" -> handler(Response.text("all good"))
  )
