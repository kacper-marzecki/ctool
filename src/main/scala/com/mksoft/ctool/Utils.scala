package com.mksoft.ctool

import zio.{IO, UIO, Task}
import io.circe._, io.circe.parser._, io.circe.syntax._
import cats.data._
import cats.implicits._
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
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
import com.mksoft.ctool.Model._

object Utils {
  def ex(errorMsg: String)                       = new LogicError(errorMsg)
  def failEx[E](errorMsg: String): Task[Nothing] = zio.ZIO.fail(ex(errorMsg))
  val ignore                                     = (_: Any) => ()
  def foldToJson[A](
      eff: Eff[A]
  )(implicit contentEncoder: Encoder[A]): Eff[ApiResponse[String, A]] = {
    eff
      .fold[ApiResponse[String, A]](
        {
          case LogicError(msg) => ApiError(msg)
          case other =>{
            val ex = other
            InternalError(
              (ex :: ex.getStackTrace.toList)
              .map(_.toString).intercalate("\n")
            )
          }
        },
        ApiSuccess(_)
      )
  }
}
