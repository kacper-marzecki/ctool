package com.mksoft.ctool

import java.sql.Timestamp

import zio._
import zio.blocking.Blocking
import zio.process.CommandError
import zio.stream.ZStream

object Model {
  type Result[+L, +R]    = ZIO[ZEnv, L, R]
  type Eff[+A]           = Result[Throwable, A]
  type CommandLineStream = ZStream[Blocking, CommandError, String]
}

sealed trait AppCommand;
case class StartServer() extends AppCommand
case class Exec(command: String, dir: String, args: List[String])
    extends AppCommand
case class ExecStored(command: String)    extends AppCommand
case class ExecScala(commandName: String) extends AppCommand

case class LogicError(msg: String) extends RuntimeException(msg)

// Entities
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

// Requests
case class SaveStoredCommandIn(
    name: String,
    command: String,
    options: List[String],
    dir: String
)

sealed trait ApiResponse[+E, +A]
case class ApiSuccess[+E, +A](content: A, status: String = "success")
    extends ApiResponse[E, A]
case class ApiError[+E, +A](cause: E, status: String = "error")
    extends ApiResponse[E, A]
case class InternalError[+E, +A](stackTrace: String) extends ApiResponse[E, A]
