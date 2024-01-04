package example.services

import zio.*
import example.models.*

trait UserService:
  def exists(id: UserId): Task[Boolean]
  def createUser(name: String): Task[User]
  def getUser(id: UserId): Task[Option[User]]
  def updateUser(id: UserId, updateUser: UpdateUser): Task[Option[User]]
  def getAll(): Task[List[User]]
  def deleteUser(id: UserId): Task[Unit]
