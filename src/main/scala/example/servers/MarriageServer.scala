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
    marriageStatus(userA).zip(marriageStatus(userB)).flatMap {
      case (Some(ma), _) =>
        ZIO.fail(AppError.AlreadyMarriedError(ma.userA, ma.userB))
      case (_, Some(mb)) =>
        ZIO.fail(AppError.AlreadyMarriedError(mb.userA, mb.userB))
      case (None, None) => {
        // none married, marry them now.
        Marriage
          .make(userA, userB)
          .flatMap(m =>
            dbRef
              .updateAndGet(db => db ++ Map((userA, m), (userB, m)))
              .map(_ => m)
          )
      }
    }

  override def divorce(userA: UserId, userB: UserId): Task[Unit] =
    marriageStatus(userA).zip(marriageStatus(userB)).flatMap {
      case (None, _) | (_, None) =>
        ZIO.fail(AppError.NotMarriedError(userA, userB))
      case (Some(ma), Some(mb)) => {
        if (!Set(ma.userA, ma.userB).equals(Set(mb.userA, mb.userB)))
          ZIO.fail(AppError.NotMarriedToEachOtherError(userA, userB))
        else
          dbRef.update(m => m.removedAll(List(userA, userB)))
      }
    }

  override def marriageStatus(userId: UserId): Task[Option[Marriage]] =
    dbRef.get.map(_.get(userId))

object MarriageServer:
  lazy val live: ZLayer[Ref[Map[UserId, Marriage]], Nothing, MarriageService] =
    ZLayer.fromFunction(MarriageServer.apply)
