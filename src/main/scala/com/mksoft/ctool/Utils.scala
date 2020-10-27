package com.mksoft.ctool

import zio.{IO, UIO, Task}
import io.circe._, io.circe.parser._, io.circe.syntax._
import cats.data._
import cats.implicits._
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
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
        case InternalError(stackTrace) =>
          Json.obj(
            ("status", Json.fromString("INTERNAL SERVER ERROR")),
            ("cause", Json.fromString(stackTrace))
          )
      }
    }
  }
}

object Utils {
  import Encoders.apiResponseEncoder
  def ex(errorMsg: String)                       = new LogicError(errorMsg)
  def failEx[E](errorMsg: String): Task[Nothing] = zio.ZIO.fail(ex(errorMsg))
  val ignore                                     = (_: Any) => ()
  def foldToJson[E, A](
      exit: zio.Exit[E, A]
  )(implicit contentEncoder: Encoder[A]): (String, StatusCode) = {
    val apiResponse: ApiResponse[String, A] =
      exit.toEither.fold(
        {
          case LogicError(msg) => println("logic") ; ApiError(msg)
          case _ @it => println("internal"); println(it)
            InternalError(
              it.getStackTrace().toList.map(_.toString).intercalate("\n")
            )
        },
        ApiSuccess(_)
      )
    val responseStatus = apiResponse match {
      case InternalError(stackTrace) => InternalServerError
      case _                         => OK
    }
    (apiResponse.asJson.noSpaces, responseStatus)
  }
}
