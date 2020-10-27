package com.mksoft.ctool

import java.sql.Timestamp

import com.mksoft.ctool.Model.{CommandLineStream, Eff}
import com.mksoft.ctool.Utils.ex
import zio._
import zio.interop.catz._
import zio.ZIO._
import zio.blocking.Blocking
import zio.console._
import zio.process.CommandError
import zio.stream.ZStream
import cats.data._
import cats.implicits._

object Service {
  def persistUse(
      persistCommand: String ⇒ Eff[Unit],
      persistDir: String ⇒ Eff[Unit],
      persistArgs: (String, List[String]) ⇒ Eff[Unit],
      incrementCommandUse: String ⇒ Eff[Unit],
      incrementDirUse: String ⇒ Eff[Unit],
      incrementArgsUse: (String, List[String]) ⇒ Eff[Unit],
      persistCommandExecution: (CommandExecutionE) ⇒ Eff[Unit],
      getCurrentTime: Eff[Timestamp]
  )(exec: Exec): Eff[Unit] = {
    persistCommand(exec.command) *>
      persistDir(exec.dir) *>
      persistArgs(exec.command, exec.args) *>
      incrementCommandUse(exec.command) *>
      incrementDirUse(exec.dir) *>
      incrementArgsUse(exec.command, exec.args) *>
      (for {
        timestamp ← getCurrentTime
        _ ← persistCommandExecution(
          CommandExecutionE(
            commandString = exec.command,
            dir = exec.dir,
            args = exec.args.intercalate(";;;"),
            time = timestamp
          )
        )
      } yield ())
  }

  def execScalaCommand(commandName: String): Eff[Any] = {
    commandName match {
      case _ ⇒ Utils.failEx(s"No scala command found with name $commandName")
    }
  }

  def executeCommand(
      executeExec: (Exec) ⇒ Eff[CommandLineStream],
      getStoredCommand: (String) ⇒ Eff[StoredCommandE],
      incrementStoredCommandUse: (String) ⇒ Eff[Unit]
  )(commandName: String) = {
    for {
      stored ← getStoredCommand(commandName)
      stream ← executeExec(toExec(stored))
      _      ← incrementStoredCommandUse(commandName)
    } yield stream
  }

  def executeExec(persistUse: (Exec) ⇒ Eff[Unit])(
      exec: Exec
  ): Eff[CommandLineStream] = {
    val command = CommandParser
      .command(exec)
      .run
      .bimap(
        _.getCause,
        { res ⇒ res.stderr.linesStream.concat(res.stdout.linesStream) }
      )
    for {
      stream ← command
      _      ← persistUse(exec)
    } yield stream
  }

  def getStoredCommand(
      getByName: (String) => Eff[Option[StoredCommandE]]
  )(id: String) = {
    getByName(id)
      .flatMap(
        fromOption(_)
          .orElseFail(ex(s"Cannot Find Command with id: $id"))
      )
  }

  def saveStoredCommand(
      persistCommand: String ⇒ Eff[Unit],
      persistDir: String ⇒ Eff[Unit],
      persistArgs: (String, List[String]) ⇒ Eff[Unit],
      getByName: (String) => Eff[Option[StoredCommandE]],
      saveStoredCommand: (StoredCommandE) => Eff[Unit]
  )(in: SaveStoredCommandIn): Eff[Unit] = {
    val validation = for {
      maybeStoredCommand <- getByName(in.name)
      exists = !maybeStoredCommand.isEmpty
      _ <-
        if (exists) {
          Utils.failEx(s"Command with name ${in.name} already Exists")
        } else { ZIO.succeed(()) }
    } yield ()

    validation *>
      saveStoredCommand(
        StoredCommandE(
          name = in.name,
          commandString = in.command,
          args = in.options.intercalate(";;;"),
          dir = in.dir,
          uses = 0
        )
      ) *>
      persistCommand(in.command) *>
      persistDir(in.dir) *>
      persistArgs(in.command, in.options)
  }

  def getTopCommands(getTopCommands: Eff[List[CommandE]]) =
    for {
      commands <- getTopCommands
    } yield commands.map(_.commandString)

  def getTopDirectories(getTopDirectories: Eff[List[DirectoryE]]) =
    for {
      directories <- getTopDirectories
    } yield directories.map(_.dir)

  def getTopArgs(
      getTopArgsForCommand: String => Eff[List[String]]
  )(command: String): Eff[List[String]] =
    for {
      args <- getTopArgsForCommand(command)
    } yield args

  def toExec(s: StoredCommandE) =
    Exec(s.commandString, s.dir, s.args.split(";;;").toList)
}
