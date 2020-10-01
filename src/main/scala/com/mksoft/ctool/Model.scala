package com.mksoft.ctool

import java.sql.Timestamp

import zio._
import zio.blocking.Blocking
import zio.process.CommandError
import zio.stream.ZStream

object Model {
  type Result[+L, +R] = ZIO[ZEnv, L, R]
  type Eff[+A] = Result[Throwable, A]
  type CommandLineStream = ZStream[Blocking, CommandError, String]
}

abstract sealed class AppCommand;

case class Server() extends AppCommand
case class Exec(command: String, dir: String, args: List[String])
    extends AppCommand
case class ExecStored(command: String) extends AppCommand
case class ExecScala(commandName: String) extends AppCommand

case class DirectoryE(dir: String, uses: Int)
case class CommandE(commandString: String, uses: Int)
case class CommandArgE(commandString: String, arg: String, uses: Int)

case class CommandExecutionE(
    time: Timestamp,
    commandString: String,
    args: String,
    dir: String
)

case class StoredCommandE(
    name: String,
    commandString: String,
    args: String,
    dir: String,
    uses: Int
)
