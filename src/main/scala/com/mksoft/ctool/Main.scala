package com.mksoft.ctool

import java.io.File

import zio.ZIO.{fail, succeed, _}
import zio.console.{Console, putStrLn, putStrLnErr}
import zio.process.Command
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import cats.implicits._
import cats._
import Utils._
import doobie._
import cats.data._
import cats.implicits._
import doobie.implicits._
import cats.implicits._
import com.mksoft.ctool.Model._
import doobie.free.{databasemetadata => DMD}

object Main extends zio.App {
  val xa = Repository.xa()

  val getCommand = Service.getCommand(Repository.getCommandQ(xa))(_)

  def runCommand(command: AppCommand): Eff[Any] = {
    command match {
      case Server() => ???
      case exec: Exec =>
        CommandParser
          .command(exec)
          .run
          .map(res => {
            res.stderr.linesStream.concat(res.stdout.linesStream)
          })
          .flatMap(lines => {
            lines.foreach(it => putStrLn(it))
          })
      case ExecStored(command) => getCommand(command).flatMap(runCommand)
    }
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    (for {
      _ <- Repository.migrate(xa)
      command <- CommandParser.parseCommand(args)
      _ <- runCommand(command)
    } yield ())
      .fold(err => ZIO(err.printStackTrace()), _ => succeed())
      .flatten
      .exitCode
}
