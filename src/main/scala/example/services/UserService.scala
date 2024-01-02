package example.services

import zio.*
import example.models.*

trait UserService:
  def createUser(name: String): Task[User]
  def getUser(id: UserId): Task[Option[User]]
  def getAll(): Task[List[User]]
  def deleteUser(id: UserId): Task[Unit]
