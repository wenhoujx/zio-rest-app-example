package example.servers

import example.services.UserService
import example.servers.*
import example.models.*

import zio.*
import zio.test.*
import zio.test.Assertion.*

object UserServerSpec extends ZIOSpecDefault:
  override def spec: Spec[TestEnvironment, Throwable] = {
    suite("UserServer")(
      test("create User then get") {
        for
          userService <- ZIO.service[UserService]
          user <- userService.createUser("wen")
          get <- userService.getUser(user.id)
        yield assert(Some(user))(equalTo(get))
      },
      test("get all") {
        for
          userService <- ZIO.service[UserService]
          user1 <- userService.createUser("Wen")
          user2 <- userService.createUser("Test")
          allUsers <- userService.getAll()
        yield assert(allUsers)(hasSize(equalTo(2))) && assert(allUsers)(
          hasSameElements(List(user1, user2))
        )
      },
      test("update") {
        for
          userService <- ZIO.service[UserService]
          user1 <- userService.createUser("Wen")
          user2 <- userService.createUser("Test")
          _ <- userService.updateUser(user1.id, UpdateUser(Some("WWen")))
          updatedUser <- userService.getUser(user1.id)
        yield assert(updatedUser)(
          // this assert is not necessary, but good to show off zio-test
          hasField(
            "name",
            (u: Option[User]) => u.map(_.name),
            equalTo(Some("WWen"))
          )
            && hasField(
              "id",
              (u: Option[User]) => u.map(_.id),
              equalTo(Some(user1.id))
            )
        )
      },
      test("delete User") {
        for
          userService <- ZIO.service[UserService]
          user <- userService.createUser("wen")
          _ <- userService.deleteUser(user.id)
          allUsers <- userService.getAll()
        yield assert(allUsers)(isEmpty)
      }
    )
  }.provide(
    ZLayer.fromZIO(Ref.make(Map.empty[UserId, User])),
    InRamUserServer.live
  )
