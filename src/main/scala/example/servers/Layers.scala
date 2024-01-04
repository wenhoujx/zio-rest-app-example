package example.servers

import zio.*
import example.models.*

object layers:
  val marriageDbLayer: ZLayer[Any, Nothing, Ref[Map[UserId, Marriage]]] =
    ZLayer.fromZIO(Ref.make(Map.empty[UserId, Marriage]))
  val userDbLayer: ZLayer[Any, Nothing, Ref[Map[UserId, User]]] =
    ZLayer.fromZIO(Ref.make(Map.empty[UserId, User]))
