package com.mksoft.ctool

import com.mksoft.ctool.Model.Eff
import zio._
import zio.ZIO._

object Service {
  def getCommand(getById: (String) => Eff[Option[String]])(id: String) = {
//    getById(id).flatMap(ZIO.fromOption)
    ZIO.succeed(Exec(".", "ls", List("-l")))
  }
}
