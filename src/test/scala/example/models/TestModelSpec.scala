package example.models

import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.json.*

object TestModelsSpec extends ZIOSpecDefault:
  override def spec = suite("TestModels")(
    test("bogus test") {
      for {
        res <- ZIO.succeed(42)
      } yield assert(res)(equalTo(42))
    },
    test("user round-trip") {
      for
        user <- User.make("wen")
        newUser = user.toJson.fromJson[User]
      yield assert(Right(user))(equalTo(newUser))
    }
  )
