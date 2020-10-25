import sbt.Keys.mainClass

import scala.sys.process.Process

val zioVersion      = "1.0.1"
val AkkaVersion     = "2.6.8"
val AkkaHttpVersion = "10.2.0"

organization := "com.mksoft"
scalaVersion := "2.13.3"
version := "1.0"
fork := true
mainClass in (Compile, run) := Some("com.mksoft.ctool.Main")
name := "ctool"
assembly := assembly.dependsOn(copyWebapp).value

lazy val uiProjectName = "ui"
lazy val uiDirectory   = settingKey[File]("Path to the ui project directory")
lazy val yarnBuild     = taskKey[Unit]("Run yarn build")
lazy val copyWebapp    = taskKey[Unit]("Copy webapp")
uiDirectory := baseDirectory.value / uiProjectName
yarnBuild := {
  streams.value.log("Running yarn build")
  Process("yarn build", uiDirectory.value).!
}
copyWebapp := {
  streams.value.log.info("Copying the webapp resources")
  IO.copyDirectory(
    uiDirectory.value / "build",
    (classDirectory in Compile).value / "webapp"
  )
}
copyWebapp := copyWebapp.dependsOn(yarnBuild).value

libraryDependencies ++= Seq(
  "org.typelevel"     %% "cats-core"        % "2.1.1" ,
  "org.typelevel"     %% "cats-effect"      % "2.2.0",
  "dev.zio"           %% "zio"              % zioVersion,
  "dev.zio"           %% "zio-streams"      % zioVersion,
  "dev.zio"           %% "zio-interop-cats" % "2.1.4.0",
  "dev.zio"           %% "zio-process"      % "0.1.0",
  "org.xerial"         % "sqlite-jdbc"      % "3.32.3.2",
  "org.tpolecat"      %% "doobie-core"      % "0.9.0",
  "org.tpolecat"      %% "doobie-hikari"    % "0.9.0",
  "org.tpolecat"      %% "doobie-specs2"    % "0.9.0",
  "com.typesafe.akka" %% "akka-stream"      % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http"        % AkkaHttpVersion
)
