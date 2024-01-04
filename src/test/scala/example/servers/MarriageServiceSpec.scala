package example.servers

import example.services.MarriageService
import example.servers.*
import example.models.*

import scala.jdk.CollectionConverters.*
import zio.*
import zio.test.*
import zio.test.Assertion.*
import example.errors.AppError
import example.routes.*

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
        layers.userDbLayer,
        InRamUserServer.live,
        MarriageServer.live,
        MarriageRoutes.live,
        UserRoutes.live
      )
    )
  def createUsers(n: Int): ZIO[UserRoutes, Throwable, List[User]] = {
    ZIO.loop(0)(_ < n, _ + 1)(n =>
      ZIO.serviceWithZIO[UserRoutes](_.service.createUser(s"user-$n"))
    )
  }
  val getMarriageService: ZIO[MarriageRoutes, Throwable, MarriageService] =
    ZIO.environmentWith(_.get.service)

  lazy val allTests = suite("test marriage service happy path")(
    test("create marriage") {
      for
        marriageService <- getMarriageService
        users <- createUsers(2)
        (id1, id2) = (users(0).id, users(1).id)
        m <- marriageService.marry(id1, id2)
      yield assert(m.userA)(isOneOf(List(id1, id2))) &&
        assert(m.userB)(isOneOf(List(id1, id2)))
    },
    test("query marriage status") {
      for
        marriageService <- getMarriageService
        users <- createUsers(2)
        List(id1, id2) = (users.take(2)).map(_.id)
        m <- marriageService.marry(id1, id2)
        getM1 <- marriageService.marriageStatus(id1)
        getM2 <- marriageService.marriageStatus(id2)
      yield assert(Some(m))(equalTo(getM1)) && assert(Some(m))(equalTo(getM2))
    },
    test("test divorce") {
      for
        marriageService <- getMarriageService
        users <- createUsers(2)
        List(id1, id2) = users.map(_.id)
        m <- marriageService.marry(id1, id2)
        _ <- marriageService.divorce(id1, id2)
        m1 <- marriageService.marriageStatus(id1)
        m2 <- marriageService.marriageStatus(id2)
      yield assert(m1)(isNone) && assert(m2)(isNone)
    },
    test("self marry banned") {
      val v = for
        marriageService <- getMarriageService
        id1 <- createUsers(1).map(_.head.id)
        res <- marriageService.marry(id1, id1)
      yield res
      assertZIO(v.exit)(fails(equalTo(AppError.CannotSelfMarryError)))
    },
    test("can't marry one that's already married") {
      val v = for
        marriageService <- getMarriageService
        users <- createUsers(3)
        List(id1, id2, id3) = users.map(_.id)
        _ <- marriageService.marry(id1, id2)
        res <- marriageService.marry(id1, id3)
      yield res
      assertZIO(v.exit)(
        fails(isSubtype[AppError.AlreadyMarriedError](anything))
      )
    },
    test("divorce unmarried couple") {
      val v = for
        marriageService <- getMarriageService
        users <- createUsers(2)
        List(id1, id2) = users.map(_.id)
        m <- marriageService.divorce(id1, id2)
      yield m
      assertZIO(v.exit)(
        fails(isSubtype[AppError.NotMarriedError](anything))
      )
    },
    test("can't remarry the same person") {
      val v = for
        marriageService <- getMarriageService
        users <- createUsers(2)
        List(id1, id2) = users.map(_.id)
        _ <- marriageService.marry(id1, id2)
        m <- marriageService.marry(id1, id2)
      yield m
      assertZIO(v.exit)(
        fails(isSubtype[AppError.AlreadyMarriedError](anything))
      )
    },
    test("missing user can't get married") {
      val v = for
        marriageService <- getMarriageService
        id <- Random.nextUUID.map(UserId(_))
        m <- marriageService.marry(id, id)
      yield m
      assertZIO(v.exit)(
        fails(isSubtype[AppError.MissingUserError](anything))
      )
    },
    test("missing user can't get divorce") {
      val v = for
        marriageService <- getMarriageService
        id <- Random.nextUUID.map(UserId(_))
        m <- marriageService.divorce(id, id)
      yield m
      assertZIO(v.exit)(
        fails(isSubtype[AppError.MissingUserError](anything))
      )
    },
    test("missing user can't get marriage status") {
      val v = for
        marriageService <- getMarriageService
        id <- Random.nextUUID.map(UserId(_))
        m <- marriageService.marriageStatus(id)
      yield m
      assertZIO(v.exit)(
        fails(isSubtype[AppError.MissingUserError](anything))
      )
    }
  )
