package com.mksoft.ctool.server

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher._
import akka.stream.scaladsl.{Flow, Sink}
import com.mksoft.ctool.CompositionRoot
object WebsocketRoutes {

  def apply(compositionRoot: CompositionRoot) = {
    val wsFlow: Flow[Message, Message, Any] =
      Flow.fromSinkAndSourceCoupled(
        Sink.ignore,
        compositionRoot.wsSource
          .map(message => {
            println(s"received: $message")
            TextMessage(message)
          })
      )
    pathPrefix("ws") {
      get {
        handleWebSocketMessages(wsFlow)
      }
    }
  }
}
