package com.mksoft.ctool

import zio.{IO, UIO, Task}

object Utils {
  def failEx[E](error: String): Task[Nothing] = zio.ZIO.fail(new RuntimeException(error))
  val ignore = (_ : Any) => ()
}
