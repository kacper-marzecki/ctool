package com.mksoft.ctool

import java.sql.Timestamp
import java.time.LocalDateTime

import com.mksoft.ctool.Model._
import zio.ZIO.succeed
import zio._
import zio.console.putStrLn

object Main extends zio.App {
  val xa = Repository.xa()

  val getCurrentTime: Eff[Timestamp] = ZIO(
    java.sql.Timestamp.valueOf(LocalDateTime.now())
  )

  val getCommand =
    Service.getStoredCommand(Repository.getCommandQ(xa))(_: String)

  val persistUse = Service.persistUse(
    persistCommand = Repository.persistCommandQ(xa)(_),
    persistDir = Repository.persistDirQ(xa)(_),
    persistArgs = Repository.persistArgsQ(xa)(_, _),
    incrementCommandUse = Repository.incrementCommandUseQ(xa)(_),
    incrementDirUse = Repository.incrementDirUseQ(xa)(_),
    incrementArgsUse = Repository.incrementArgsUseQ(xa)(_, _),
    persistCommandExecution = Repository.persistCommandExecutionQ(xa)(_),
    getCurrentTime = getCurrentTime
  )(_)

  val executeExec = Service.executeExec(persistUse)(_)

  val executeCommand = Service.executeCommand(
    executeExec,
    getCommand,
    Repository.incrementStoredCommandUseQ(xa)(_)
  )(_)

  def runCommand(command: AppCommand): Eff[Any] = {
    command match {
      case Server() => ???
      case exec: Exec =>
        executeExec(exec)
          .flatMap(lines => {
            lines.foreach(putStrLn(_))
          })
      case ExecStored(command) =>
        executeCommand(command).flatMap(lines => {
          lines.foreach(putStrLn(_))
        })
      case ExecScala(commandName) ⇒ Service.execScalaCommand(commandName)
    }
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    (for {
      _ <- RepositorySetup.migrate(xa)
      command <- CommandParser.parseCommand(args)
      _ <- runCommand(command)
    } yield ())
      .fold(err => ZIO(err.printStackTrace()), _ => succeed())
      .flatten
      .exitCode
}
