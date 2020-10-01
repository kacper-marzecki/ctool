package com.mksoft.ctool

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import com.mksoft.ctool.Model._
import zio.ZIO.succeed
import zio._
import zio.console.putStrLn
import akka.http.scaladsl.server.Directives._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
object Main extends zio.App {
  val root = CompositionRoot()

  def runCommand(command: AppCommand): Eff[Any] = {
    command match {
      case StartServer() => Server.run(root)
      case exec: Exec =>
        root
          .executeExec(exec)
          .flatMap(lines => {
            lines.foreach(putStrLn(_))
          })
      case ExecStored(command) =>
        root
          .executeCommand(command)
          .flatMap(lines => {
            lines.foreach(putStrLn(_))
          })
      case ExecScala(commandName) ⇒ Service.execScalaCommand(commandName)
    }
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    (for {
      _ <- RepositorySetup.migrate(root.xa)
      command <- CommandParser.parseCommand(args)
      _ <- runCommand(command)
    } yield ())
      .fold(
        err => ZIO(err.printStackTrace()),
        _ ⇒ putStrLn("successfull command")
      )
      .flatten
      .exitCode
}
