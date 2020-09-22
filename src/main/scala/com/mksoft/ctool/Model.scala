package com.mksoft.ctool

import zio._

object Model {
  type Result[+L, +R] = ZIO[ZEnv, L, R]
  type Eff[+A] = Result[Throwable, A]
}

abstract sealed class AppCommand;

case class Server() extends AppCommand
case class Exec(dir: String, command: String, args: List[String]) extends AppCommand
case class ExecStored(command: String) extends AppCommand

