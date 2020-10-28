package com.mksoft.ctool

import akka.http.scaladsl.server.Route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatcher._
import scala.util.Failure
import scala.util.Success
import com.mksoft.ctool.Utils._
import com.mksoft.ctool.Model.Eff
import zio.Runtime
import akka.http.scaladsl.unmarshalling.Unmarshal

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import zio.ZIO
object CommandRoutes {
  def apply(compositionRoot: CompositionRoot): Route = {
    import compositionRoot._
    import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
    import io.circe.generic.auto._

    (pathPrefix("command")) {
      (get & path("top-commands")) {
        completeJson(getTopCommands)
      } ~
        (get & path("top-directories")) {
          completeJson(getTopDirectories)
        } ~
        (get & pathPrefix("top-args")) {
          path(Segment) { it =>
            completeJson(getTopArgs(it))
          }
        } ~
        (get & pathPrefix("recent")) {
          // TODO paginate
          completeJson(compositionRoot.getRecentCommands)
        } ~
        (get & pathPrefix("stored")) {
            completeJson(getStoredCommands)
        } ~
        (post & pathEndOrSingleSlash) {
          entity(as[SaveStoredCommandIn]) { it =>
            completeJson(saveStoredCommand(it))
          }
        }
    }
  }
}
