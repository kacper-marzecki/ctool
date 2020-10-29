package com.mksoft.ctool

import java.sql.Timestamp
import java.time.LocalDateTime

import com.mksoft.ctool.Model.Eff
import com.mksoft.ctool.repository.Repository
import zio.ZIO
import io.circe.Encoder
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import Encoders._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.scaladsl.Source
import akka.http.scaladsl.model.ws.Message
import akka.stream.{ActorMaterializer, CompletionStrategy, OverflowStrategy}
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink

import scala.concurrent.Future

case class CompositionRoot() {
  val xa              = Repository.xa()
  implicit val system = ActorSystem(Behaviors.empty, "ctool-system")
  val (wsActor, wsSource) = Source
      .actorRef[String](
        PartialFunction.empty,
        PartialFunction.empty,
        5000,
        OverflowStrategy.dropBuffer)
    .preMaterialize()
  val uselessSink = wsSource.runWith(Sink.ignore)
  
  val zioRuntime = zio.Runtime.default

  val getCurrentTime: Eff[Timestamp] = ZIO(
    java.sql.Timestamp.valueOf(LocalDateTime.now())
  )

  val getCommand =
    Service.getStoredCommand(Repository.getCommandQ(xa))(_: Int)

  val persistUse = Service.persistUse(
    persistCommand = Repository.persistCommandQ(xa)(_),
    persistDir = Repository.persistDirQ(xa)(_),
    persistArgs = Repository.persistArgsQ(xa)(_, _),
    incrementCommandUse = Repository.incrementCommandUseQ(xa)(_),
    incrementDirUse = Repository.incrementDirUseQ(xa)(_),
    incrementArgsUse = Repository.incrementArgsUseQ(xa)(_, _),
    persistCommandExecution = Repository.persistCommandExecutionQ(xa)(_),
    getCurrentTime = getCurrentTime
  )(_)

  val executeExec = Service.executeExec(persistUse)(_)

  val executeStoredCommand = Service.executeStoredCommand(
    executeExec,
    getCommand,
    Repository.incrementStoredCommandUseQ(xa)(_)
  )(_)

  val sendWsMsg = (s: String) => {
    wsActor ! s
    println(s)
  }

  val executeStoredCommandAndStreamOutput = Service.executeStoredCommandAndStreamOutput(
    executeStoredCommand,
    sendWsMsg
  )(_)

  val saveStoredCommand = Service.saveStoredCommand(
    persistCommand = Repository.persistCommandQ(xa)(_),
    persistDir = Repository.persistDirQ(xa)(_),
    persistArgs = Repository.persistArgsQ(xa)(_, _),
    Repository.getCommandByNameQ(xa),
    Repository.saveStoredCommandQ(xa)(_)
  )(_: SaveStoredCommandIn)

  val getStoredCommands =
    Service.getStoredCommands(Repository.getStoredCommandsQ(xa))
  val getRecentCommands: Eff[List[CommandExecutionOut]] =
    Service.getRecentCommands(Repository.getRecentCommandsQ(xa))
  val getTopCommands = Service.getTopCommands(Repository.getTopCommandsQ(xa))
  val getTopDirectories =
    Service.getTopDirectories(Repository.getTopDirectoriesQ(xa))

  def getTopArgs(command: String) = {
    Service.getTopArgs(Repository.getTopArgsForCommandQ(xa)(_))(command)
  }

  def completeJson[A](eff: Eff[A])(implicit ec: Encoder[A]) = {
    val a: zio.Exit[Throwable, ApiResponse[String, A]] = zioRuntime
      .unsafeRunSync(Utils.foldToJson(eff))
    a.fold(
      it => complete(InternalServerError, "Unhandled"),
      it => {
        it match {
          case InternalError(stackTrace) =>
            complete(InternalServerError, stackTrace)
          case _ @response => complete(OK, response.asJson.noSpaces)
        }
      }
    )
  }
}
