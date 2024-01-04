package example.servers

import example.services.MarriageService
import example.servers.*
import example.models.*

import scala.jdk.CollectionConverters.*
import zio.*
import zio.test.*
import zio.test.Assertion.*
import example.errors.AppError

object MarriageServiceSpec extends ZIOSpecDefault:
  // somehow sbt compiler can't get the types during runtime.
  val mapLayer: ZLayer[Any, Nothing, Ref[Map[UserId, Marriage]]] =
    ZLayer.fromZIO(
      Ref.make[Map[UserId, Marriage]](Map.empty)
    )
  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("MarriageServiceSpec")(
      allTests.provide(
        layers.marriageDbLayer,
        MarriageServer.live
      )
    )

  lazy val allTests = suite("test marriage service happy path")(
    test("create marriage") {
      for
        marriageService <- ZIO.service[MarriageService]
        id1 <- UserId.random
        id2 <- UserId.random
        m <- marriageService.marry(id1, id2)
      yield assert(m.userA)(isOneOf(List(id1, id2))) &&
        assert(m.userB)(isOneOf(List(id1, id2)))
    },
    test("query marriage status") {
      for
        marriageService <- ZIO.service[MarriageService]
        id1 <- UserId.random
        id2 <- UserId.random
        m <- marriageService.marry(id1, id2)
        getM1 <- marriageService.marriageStatus(id1)
        getM2 <- marriageService.marriageStatus(id2)
      yield assert(Some(m))(equalTo(getM1)) && assert(Some(m))(equalTo(getM2))
    },
    test("test divorce") {
      for
        marriageService <- ZIO.service[MarriageService]
        id1 <- UserId.random
        id2 <- UserId.random
        m <- marriageService.marry(id1, id2)
        _ <- marriageService.divorce(id1, id2)
        m1 <- marriageService.marriageStatus(id1)
        m2 <- marriageService.marriageStatus(id2)
      yield assert(m1)(isNone) && assert(m2)(isNone)
    },
    test("self marry banned") {
      val v = for
        marriageService <- ZIO.service[MarriageService]
        id1 <- UserId.random
        res <- marriageService.marry(id1, id1)
      yield res
      assertZIO(v.exit)(fails(equalTo(AppError.CannotSelfMarryError)))
    },
    test("can't marry one that's already married") {
      val v = for
        marriageService <- ZIO.service[MarriageService]
        id1 <- UserId.random
        id2 <- UserId.random
        id3 <- UserId.random
        _ <- marriageService.marry(id1, id2)
        res <- marriageService.marry(id1, id3)
      yield res
      assertZIO(v.exit)(
        fails(isSubtype[AppError.AlreadyMarriedError](anything))
      )
    },
    test("can't remarry the same person") {
      val v = for
        marriageService <- ZIO.service[MarriageService]
        id1 <- UserId.random
        id2 <- UserId.random
        _ <- marriageService.marry(id1, id2)
        m <- marriageService.marry(id1, id2)
      yield m
      assertZIO(v.exit)(
        fails(isSubtype[AppError.AlreadyMarriedError](anything))
      )

    }
  )

  lazy val missingUserPath = suite("test missing users path")(
    test("test empty marriage status for unknown user") {
      val v = for
        marriageService <- ZIO.service[MarriageService]
        id1 <- UserId.random
        res <- marriageService.marriageStatus(id1)
      yield res
      assertZIO(v.exit)(fails(isSubtype[AppError.MissingUserError](anything)))
    }
  )
