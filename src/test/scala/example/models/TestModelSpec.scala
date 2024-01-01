package example.models

import zio.*
import zio.test.*
import zio.test.Assertion.*

object TestModelsSpec extends ZIOSpecDefault:
  override def spec = suite("TestModels")(
    test("bogus test") {
      for {
        res <- ZIO.succeed(42)
      } yield assert(res)(equalTo(42))
    }
  )
