package example.servers

import example.services.UserService
import example.services.MarriageService
import example.servers.*
import example.models.*

import zio.*
import zio.test.*
import zio.test.Assertion.*
import example.routes.*
import example.errors.AppError

object UserServerSpec extends ZIOSpecDefault:
  val getUserService: ZIO[UserRoutes, Throwable, UserService] =
    ZIO.environmentWith(_.get.service)
  override def spec: Spec[TestEnvironment, Throwable] = {
    suite("UserServer")(
      test("create User then get") {
        for
          userService <- getUserService
          user <- userService.createUser("wen")
          get <- userService.getUser(user.id)
        yield assert(Some(user))(equalTo(get))
      },
      test("get all") {
        for
          userService <- getUserService
          user1 <- userService.createUser("Wen")
          user2 <- userService.createUser("Test")
          allUsers <- userService.getAll()
        yield assert(allUsers)(hasSize(equalTo(2))) && assert(allUsers)(
          hasSameElements(List(user1, user2))
        )
      },
      test("update") {
        for
          userService <- getUserService
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
          userService <- getUserService
          user <- userService.createUser("wen")
          _ <- userService.deleteUser(user.id)
          allUsers <- userService.getAll()
        yield assert(allUsers)(isEmpty)
      },
      test("can't delete married user") {
        val v = for
          userService <- getUserService
          marriageService <- ZIO.service[MarriageService]
          user1 <- userService.createUser("user1")
          user2 <- userService.createUser("user2")
          m <- marriageService.marry(user1.id, user2.id)
          res <- userService.deleteUser(user1.id)
        yield res
        assertZIO(v.exit)(
          fails(isSubtype[AppError.CannotDeleteMarriedUserError](anything))
        )

      }
    )
  }.provide(
    layers.userDbLayer,
    InRamUserServer.live,
    layers.marriageDbLayer,
    MarriageServer.live,
    UserRoutes.live
  )
