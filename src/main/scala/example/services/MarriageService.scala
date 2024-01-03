package example.services

import example.models.*
import zio.*

trait MarriageService:
  def marry(userA: UserId, userB: UserId): Task[Marriage]
  def divorce(userA: UserId, userB: UserId): Task[Unit]
  def marriageStatus(userId: UserId): Task[Option[Marriage]]
