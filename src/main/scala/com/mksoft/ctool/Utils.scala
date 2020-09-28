package com.mksoft.ctool

import zio.{IO, UIO, Task}

object Utils {
  def ex(errorMsg: String) = new RuntimeException(errorMsg)
  def failEx[E](errorMsg: String): Task[Nothing] = zio.ZIO.fail(ex(errorMsg))
  val ignore = (_: Any) => ()
}
