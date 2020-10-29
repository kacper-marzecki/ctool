package com.mksoft.ctool

import java.time.LocalTime

import akka.{Done, NotUsed}
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.javadsl.HttpTerminated
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.{CompletionStrategy, OverflowStrategy}
import akka.stream.scaladsl.{Sink, Source}
import com.mksoft.ctool.Model.Eff
import zio.ZIO
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.ExceptionHandler
import com.mksoft.ctool.server.SseRoutes

object Server {
  val rejectionHandler =
    corsRejectionHandler.withFallback(RejectionHandler.default)

  val exceptionHandler = ExceptionHandler {
    case e => complete(StatusCodes.NotFound -> e.getMessage)
  }

  val handleErrors =
    handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

  def routes(compositionRoot: CompositionRoot) =
    handleErrors {
      cors() {
        pathPrefix("api") {
          CommandRoutes(compositionRoot) ~
            SseRoutes(compositionRoot)
        } ~
          (get & pathPrefix("")) {
            (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(
              StatusCodes.TemporaryRedirect
            )) {
              getFromResource("webapp/index.html")
            } ~ {
              getFromResourceDirectory("webapp")
            }
          } ~ complete(
          404,
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            "<h1>Not Found</h1>"
          )
        )
      }
    }

  def run(root: CompositionRoot): Eff[Nothing] = {
    ZIO
      .effect({
        implicit val system = root.system
        ZIO
          .fromFuture(_ ⇒
            // TODO parameterize port binding
            Http()(root.system)
              .newServerAt("localhost", 8080)
              .bind(routes(root))
          )
          .flatMap(portBinding ⇒
            ZIO.never.onInterrupt(
              ZIO.fromFuture(_ ⇒ portBinding.unbind()).ignore
            )
          )
      })
      .flatten
  }
}
