package com.mksoft.ctool

import com.mksoft.ctool.Model.Eff
import com.mksoft.ctool.Utils.ex
import zio._
import zio.interop.catz._
import zio.ZIO._
import zio.blocking.Blocking
import zio.console._
import zio.process.CommandError
import zio.stream.ZStream

object Service {

  def execScalaCommand(commandName: String): Eff[Any] = {
    commandName match {
      case _ ⇒ Utils.failEx(s"No scala command found with name $commandName")
    }
  }

  def execCommand(
      exec: Exec
  ): ZIO[Blocking, CommandError, ZStream[Blocking, CommandError, String]] =
    CommandParser
      .command(exec)
      .run
      .map(res => {
        res.stderr.linesStream.concat(res.stdout.linesStream)
      })

  def getStoredCommand(
      getById: (String) => Eff[Option[StoredCommandE]]
  )(id: String) = {
    getById(id)
      .flatMap(
        fromOption(_)
          .orElseFail(ex("Cannot Find Command"))
      )
      .map(s ⇒ Exec(s.commandString, s.dir, s.args.split(";;;").toList))
  }
}
