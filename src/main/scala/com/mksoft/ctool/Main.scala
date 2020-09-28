package com.mksoft.ctool

import com.mksoft.ctool.Model._
import zio.ZIO.succeed
import zio._
import zio.console.putStrLn

object Main extends zio.App {
  val xa = Repository.xa()

  val getCommand =
    Service.getStoredCommand(Repository.getCommandQ(xa))(_: String)

  def runCommand(command: AppCommand): Eff[Any] = {
    command match {
      case Server() => ???
      case exec: Exec =>
        Service
          .execCommand(exec)
          .flatMap(lines => {
            lines.foreach(putStrLn(_))
          })
      case ExecStored(command) => getCommand(command).flatMap(runCommand)
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
