package example.models

import zio.json.*

object MarriageRequest:
  final case class MarryRequest(userA: UserId, userB: UserId)
  final case class DivorceRequest(userA: UserId, userB: UserId)
  
  given marryCodec: JsonCodec[MarryRequest] = DeriveJsonCodec.gen[MarryRequest]
  given divorceCodec: JsonCodec[DivorceRequest] =
    DeriveJsonCodec.gen[DivorceRequest]
