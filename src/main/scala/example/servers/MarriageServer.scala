package example.servers

import example.models.*
import example.services.*
import example.errors.*
import zio.*
import scala.annotation.newMain

final case class MarriageServer(
    dbRef: Ref[Map[UserId, Marriage]]
) extends MarriageService:
  override def marry(userA: UserId, userB: UserId): Task[Marriage] =
    for
      marriedA <- marriageStatus(userA)
      marriedB <- marriageStatus(userB)
      m <-
        if (!marriedA.isEmpty) then
          ZIO.fail(
            AppError.AlreadyMarriedError(marriedA.get.userA, marriedA.get.userB)
          )
        else if (!marriedB.isEmpty) then
          ZIO.fail(
            AppError.AlreadyMarriedError(marriedB.get.userA, marriedB.get.userB)
          )
        else
          for
            newM <- Marriage.make(userA, userB)
            map <- dbRef.get
            _ <- dbRef.set(
              map ++ Map(userA -> newM, userB -> newM)
            )
          yield newM
    yield m

  override def divorce(userA: UserId, userB: UserId): Task[Unit] =
    for
      marriedA <- marriageStatus(userA)
      marriedB <- marriageStatus(userB)
      _ <-
        if (
          marriedA
            .filter(m => {
              (m.userA == userA && m.userB == userB) ||
              (m.userA == userB && m.userB == userA)
            })
            .isEmpty
        ) then ZIO.fail(AppError.NotMarriedError(userA, userB))
        else if (
          marriedB
            .filter(m => {
              (m.userA == userA && m.userB == userB) ||
              (m.userA == userB && m.userB == userA)
            })
            .isEmpty
        ) then ZIO.fail(AppError.NotMarriedError(userB, userA))
        else
          for
            map <- dbRef.get
            _ <- dbRef.set(map.removed(userA).removed(userB))
          yield ()
    yield ()

  override def marriageStatus(userId: UserId): Task[Option[Marriage]] =
    for
      map <- dbRef.get
      marriageOption <- ZIO.attempt(map.get(userId))
    yield marriageOption

object MarriageServer:
  lazy val live: ZLayer[Ref[Map[UserId, Marriage]], Nothing, MarriageService] =
    ZLayer.fromFunction(MarriageServer.apply)
