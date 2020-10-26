package com.mksoft.ctool

import zio.{IO, UIO, Task}
import io.circe._, io.circe.parser._, io.circe.syntax._

object Encoders {
  implicit def apiResponseEncoder[E, A](implicit
      errorEncoder: Encoder[E],
      contentEncoder: Encoder[A]
  ): Encoder[ApiResponse[E, A]] = { (a: ApiResponse[E, A]) =>
    {
      a match {
        case ApiError(cause, status) =>
          Json.obj(
            ("status", Json.fromString(status)),
            ("cause", errorEncoder.apply(cause))
          )
        case ApiSuccess(content, status) =>
          Json.obj(
            ("status", Json.fromString(status)),
            ("content", contentEncoder.apply(content))
          )
      }
    }
  }
}

object Utils {
  import Encoders.apiResponseEncoder
  def ex(errorMsg: String)                       = new RuntimeException(errorMsg)
  def failEx[E](errorMsg: String): Task[Nothing] = zio.ZIO.fail(ex(errorMsg))
  val ignore                                     = (_: Any) => ()
  def foldToJson[E, A](
      exit: zio.Exit[E, A]
  )(implicit contentEncoder: Encoder[A]): String = {
    val apiResponse: ApiResponse[String, A] =
      exit.fold(it => ApiError(it.prettyPrint), it => ApiSuccess(it))
    apiResponse.asJson.noSpaces
  }
}
