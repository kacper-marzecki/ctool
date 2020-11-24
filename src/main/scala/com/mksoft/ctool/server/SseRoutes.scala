package com.mksoft.ctool.server

import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher._
import com.mksoft.ctool.CompositionRoot

import scala.concurrent.duration._
import io.circe.generic.extras.auto._
import io.circe.generic.extras.Configuration
import io.circe.parser.decode, io.circe.syntax._

object SseRoutes {

  implicit val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("t")
  def apply(compositionRoot: CompositionRoot) = {
      pathPrefix("sse") {
        get {
          complete(
            compositionRoot.wsSource
              .map(_.asJson.noSpaces)
              .map(ServerSentEvent(_))
              .keepAlive(30.second, () => ServerSentEvent.heartbeat)
          )
        }
      }
  }
}
