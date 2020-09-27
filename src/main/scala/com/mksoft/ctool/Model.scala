package com.mksoft.ctool

import java.util.UUID

import zio._

object Model {
  type Result[+L, +R] = ZIO[ZEnv, L, R]
  type Eff[+A] = Result[Throwable, A]
}

abstract sealed class AppCommand;

case class Server() extends AppCommand
case class Exec(command: String, dir: String, args: List[String]) extends AppCommand
case class ExecStored(command: String) extends AppCommand



case class ExecCommandE(id: UUID, name: Option[String], command: String)
case class ExecArgE(commandId: UUID, arg: String)
case class ExecDirE(dir: String)

