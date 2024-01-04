package example.servers

import example.routes.*
import zio.http.Server
import zio.*

final case class AppServer(
    userRoutes: UserRoutes,
    healthRoutes: HealthRoutes.type,
    marriageRoutes: MarriageRoutes
):
  val allRoutes =
    userRoutes.routes ++ healthRoutes.routes ++ marriageRoutes.routes
  val run =
    for
      _ <- ZIO.logInfo("starting server")
      _ <- Server
        .serve(allRoutes.toHttpApp)
        .provide(Server.defaultWithPort(8080))
    yield ()

object AppServer:
  val live = ZLayer.fromFunction(AppServer.apply)
