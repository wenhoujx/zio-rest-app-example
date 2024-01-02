package example.app

import zio.*
import zio.http.*
import example.routes.*
import example.servers.*
import example.models.*
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters.*


object MainApp extends ZIOAppDefault {
  override def run = ZIO
    .serviceWithZIO[AppServer](_.run)
    .provide(
      InRamUserServer.live,
      UserRoutes.live,
      ZLayer.succeed((new ConcurrentHashMap[UserId, User]()).asScala), 
      AppServer.live
    )
}
