package com.mksoft.ctool

import java.io.{File, InputStream}
import java.sql.Timestamp

import com.mksoft.ctool.Model.{CommandLineStream, Eff}
import com.mksoft.ctool.Utils.{ex, ignore}
import zio._
import zio.interop.catz._
import zio.ZIO._
import zio.blocking.Blocking
import zio.console._
import zio.process.{Command, CommandError, ProcessInput}
import zio.stream.ZStream
import cats.data._
import cats.implicits._

object Service {
  def executeStoredCommandAndStreamOutput(
      executeCommand: Int => Eff[Model.CommandLineStream],
      streamLine: CommandExecutionMessage => Eff[Unit],
      getStoredCommand: Int ⇒ Eff[StoredCommandE],
      currentTime: Eff[Timestamp]
  )(commandId: Int): Eff[Unit] = {
    for {
      command     <- getStoredCommand(commandId)
      executionId <- currentTime.map(_.getTime)
      _           <- streamLine(CommandExecutionStarted(command.name, executionId))
      lineStream  <- executeCommand(commandId)
      s <-
        lineStream. foreach(line => {
          streamLine(CommandLine(executionId, line))
        }).forkDaemon
    } yield ()
  }

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
import scala.sys.process._
  def executeStoredCommand(
      executeExec: (Exec) ⇒ Eff[CommandLineStream],
      getStoredCommand: (Int) ⇒ Eff[StoredCommandE],
      incrementStoredCommandUse: (Int) ⇒ Eff[Unit]
  )(commandId: Int) = {
    for {
      stored ← getStoredCommand(commandId)
      exec= toExec(stored)
//      lines = s"${exec.command}".lazyLines
      stream ← executeExec(exec)
      _      ← incrementStoredCommandUse(commandId)
    } yield stream
  }
import cats.implicits._
  def executeExec(persistUse: (Exec) ⇒ Eff[Unit])(
      exec: Exec
  ): Eff[CommandLineStream] = {
    val c = s"""PATH=/home/omnissiah/.nvm/versions/node/v14.14.0/bin:/home/omnissiah/.sdkman/candidates/scala/current/bin:/home/omnissiah/.sdkman/candidates/sbt/current/bin:/home/omnissiah/.sdkman/candidates/java/current/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/usr/local/games:/snap/bin  && ${exec.command} ${exec.args.intercalate(" ")}"""

//    WORKS
//    println(s"""/bin/sh -c  "${c}"""")
//    ZIO.succeed(ZStream.fromIteratorEffect(ZIO.effect(s"""/bin/bash -c  "${c}" """.lazyLines_!.iterator)))
    Command("/bin/bash" , "-c", c)
      .workingDirectory(new File(exec.dir))
           .run
          .bimap(
            _.getCause,
            { res ⇒ res.stdout.linesStream.merge(res.stderr.linesStream) }
          )
//    for {
//    cq <- ZQueue.unbounded[String]
//     logger = ProcessLogger(cq.)
//    stream = ZStream.fromChunkQueue(cq.map(Chunk.single))
//    } yield stream
  }

  def getStoredCommand(
      getById: (Int) => Eff[Option[StoredCommandE]]
  )(id: Int) = {
    val  a = Chain(1).toList
    val c = a.map(_ + 2)
    getById(id)
      .flatMap(
        fromOption(_)
          .orElseFail(ex(s"Cannot Find Command with id: $id"))
      )
  }

  def getStoredCommands(getStoredCommands: Eff[List[StoredCommandE]]) = {
    val toOut = (it: StoredCommandE) => {
      StoredCommandOut(
        rowId = it.rowId,
        args = it.args.split(";;;").toList,
        commandString = it.commandString,
        dir = it.dir,
        name = it.name,
        uses = it.uses
      )
    }
    for {
      commands <- getStoredCommands
    } yield commands.map(toOut)
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
      exists = maybeStoredCommand.isDefined
      _ <-
        if (exists) {
          Utils.failEx(s"Command with name ${in.name} already Exists")
        } else {
          ZIO.succeed(())
        }
    } yield ()

    validation *>
      saveStoredCommand(
        StoredCommandE(
          rowId = 0,
          name = in.name.trim(),
          commandString = in.command.trim(),
          args = in.options.map(_.trim).filter(!_.isBlank).intercalate(";;;"),
          dir = in.dir.trim(),
          uses = 0
        )
      ) *>
      persistCommand(in.command) *>
      persistDir(in.dir) *>
      persistArgs(in.command, in.options)
  }

  def getRecentCommands(getRecentCommands: Eff[List[CommandExecutionE]]) = {
    val toOut = (it: CommandExecutionE) =>
      CommandExecutionOut(
        args = it.args.split(";;;").toList,
        commandString = it.commandString,
        dir = it.dir,
        time = it.time.getTime()
      )
    for {
      commands <- getRecentCommands
    } yield commands.map(toOut)
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
    Exec(s.commandString, s.dir, s.args.split(";;;").filter(_.nonEmpty).toList)
}
