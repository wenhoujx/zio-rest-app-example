ThisBuild / scalaVersion := "3.3.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.wenhoujx"
ThisBuild / organizationName := "example"

val slf4jVersion = "1.7.36" // logging framework
val zioHttpVersion = "2.0.0-RC9" // HTTP client library for ZIO
val zioJsonVersion = "0.3.0-RC8" // JSON serialization library for ZIO
val zioLoggingVersion = "2.0.0-RC10" // logging library for ZIO
val zioVersion =
  "2.0.0-RC6" // Scala library for asynchronous and concurrent programming

Global / onChangedBuildSource := ReloadOnSourceChanges

val sharedSettings = Seq(
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio-json" % zioJsonVersion,
    "dev.zio" %% "zio-test" % zioVersion % Test,
    "dev.zio" %% "zio-test-sbt" % zioVersion % Test
  ),
  Test / fork := true,
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "utf8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings"
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "zio-simple-rest-app",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-macros" % zioVersion,
      "io.d11" %% "zhttp" % zioHttpVersion,
      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.slf4j" % "slf4j-simple" % slf4jVersion
    )
  )
  .enablePlugins(JavaAppPackaging)
  .settings(sharedSettings)
