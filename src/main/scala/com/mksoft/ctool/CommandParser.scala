package com.mksoft.ctool

import java.io.File

import akka.http.scaladsl.server.util.TupleOps.enhanceTuple
import com.mksoft.ctool.Model.Eff
import com.mksoft.ctool.Utils.failEx
import zio.ZIO.succeed
import zio.process.Command

object CommandParser {
  def commandAndSubcommands(
      commandString: String
  ): Eff[(String, List[String])] = {
    commandString.split(' ').toList match {
      case head :: rest => succeed((head, rest.filter(!_.isBlank)))
      case _            => failEx("kek")
    }
  }

  def parseExec(subCommands: List[String]): Eff[Exec] =
    subCommands match {
      case directory :: commandString :: Nil =>
        for {
          (command, args) <- commandAndSubcommands(commandString)
        } yield Exec(command,directory, args)
      case _ => failEx("Correct use: <directory> <command>")
    }

  def parseCommand(args: List[String]): Eff[AppCommand] = {
    args match {
      case "server" :: Nil      => succeed(StartServer())
      case "raw" :: subCommands => parseExec(subCommands)
      case "exec" :: commandId :: Nil =>
        succeed(ExecStored(commandId.toInt))
      case _ => failEx("Please specify a command to use")
    }
  }

  def command(exec: Exec): Command = {
    Command("bash")
      .workingDirectory(new File(exec.dir))
  }
}
