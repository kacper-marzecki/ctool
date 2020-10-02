package com.mksoft.ctool

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.javadsl.HttpTerminated
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import com.mksoft.ctool.Model.Eff
import zio.ZIO

object Server {

  def routes(compositionRoot: CompositionRoot) =
    path("hello") {
      get {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            "<h1>It works</h1>"
          )
        )
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
