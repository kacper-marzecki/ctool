val zioVersion = "1.0.1"
val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.0"

scalaVersion := "2.13.3"
name := "ctool"
organization := "com.mksoft"
version := "1.0"
mainClass := Some("com.mksoft.ctool.Main")
fork := true

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.typelevel" %% "cats-effect" % "2.2.0",
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-interop-cats" % "2.1.4.0",
  "dev.zio" %% "zio-process" % "0.1.0",
  "org.xerial" % "sqlite-jdbc" % "3.32.3.2",
  "org.tpolecat" %% "doobie-core" % "0.9.0",
  "org.tpolecat" %% "doobie-hikari" % "0.9.0",
  "org.tpolecat" %% "doobie-specs2" % "0.9.0", // Specs2 support for typechecking statements.// HikariCP transactor.
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
)
