package example.app

import zio.*
import zio.http.*
import example.routes.*
import example.servers.*
import example.models.*

object MainApp extends ZIOAppDefault {
  override def run = ZIO
    .serviceWithZIO[AppServer](_.run)
    .provide(
      InRamUserServer.live,
      UserRoutes.live,
      HealthRoutes.live,
      layers.userDbLayer,
      layers.marriageDbLayer,
      MarriageServer.live,
      AppServer.live
    )
}
