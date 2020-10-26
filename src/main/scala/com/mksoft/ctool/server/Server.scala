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

object Server {
  val s = Source.actorRef(
    completionMatcher = {
      case Done =>
        // complete stream immediately if we send it Done
        CompletionStrategy.immediately
    },
    // never fail the stream because of a message
    failureMatcher = PartialFunction.empty,
    bufferSize = 100,
    overflowStrategy = OverflowStrategy.dropHead
  )
  // val a = s.to(Sink.foreach(println)).run()
    val rejectionHandler = corsRejectionHandler.withFallback(RejectionHandler.default)

    // Your exception handler
    val exceptionHandler = ExceptionHandler { case e: NoSuchElementException =>
      complete(StatusCodes.NotFound -> e.getMessage)
    }

    // Combining the two handlers only for convenience
    val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

  def routes(compositionRoot: CompositionRoot) =
   handleErrors {
      cors() {
      pathPrefix("api") {
        path("sse") {
          import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
          import scala.concurrent.duration._
          import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME
          get {
            complete(
              Source
                .tick(2.seconds, 2.seconds, NotUsed)
                .map(_ => LocalTime.now())
                .map(time => ServerSentEvent(ISO_LOCAL_TIME.format(time)))
                .keepAlive(20.second, () => ServerSentEvent.heartbeat)
            )
          }
        } ~
          CommandRoutes(compositionRoot) ~
          get {
            complete(
              404,
              HttpEntity(
                ContentTypes.`text/html(UTF-8)`,
                "<h1>Not Found</h1>"
              )
            )
          }
      } ~
        (get & pathPrefix("")) {
          (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(
            StatusCodes.TemporaryRedirect
          )) {
            getFromResource("webapp/index.html")
          } ~ {
            getFromResourceDirectory("webapp")
          }
        }
    }
   }

  def run(root: CompositionRoot): Eff[Nothing] = {
    ZIO
      .effect({
        implicit val system = ActorSystem(Behaviors.empty, "ctool-system")
        ZIO
          .fromFuture(_ ⇒
            // TODO parameterize port binding
            Http()(system).newServerAt("localhost", 8080).bind(routes(root))
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
