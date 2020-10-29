package com.mksoft.ctool.server

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher._
import akka.stream.scaladsl.{Flow, Sink}
import com.mksoft.ctool.CompositionRoot
import akka.http.scaladsl.model.sse.ServerSentEvent
import scala.concurrent.duration._
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._

object SseRoutes {

  def apply(compositionRoot: CompositionRoot) = {
      pathPrefix("sse") {
        get {
          complete(
            compositionRoot.wsSource
              .map(ServerSentEvent(_))
              .keepAlive(30.second, () => ServerSentEvent.heartbeat)
          )
        }
      }
  }
}
