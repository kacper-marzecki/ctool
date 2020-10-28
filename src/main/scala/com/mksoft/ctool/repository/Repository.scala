package com.mksoft.ctool.repository

import java.sql.Timestamp

import cats.implicits._
import com.mksoft.ctool.Model._
import com.mksoft.ctool.Utils.ignore
import doobie.implicits._
import zio.console._
import zio.interop.catz._
import zio.{UIO, ZIO}
import doobie._
import scala.io.{BufferedSource, Source}
import doobie.Fragments.{in, andOpt, whereAndOpt}
import doobie.implicits.javasql._
import doobie.implicits.javatime._
import com.mksoft.ctool.CommandExecutionE
import com.mksoft.ctool.StoredCommandE
import com.mksoft.ctool.CommandE
import com.mksoft.ctool.DirectoryE

object Repository {
  def xa() =
    Transactor.fromDriverManager[Eff](
      "org.sqlite.JDBC",
      "jdbc:sqlite:ctool.db",
      "",
      ""
    )
  import cats._, cats.data._, cats.implicits._
  import doobie._, doobie.implicits._

  def persistCommandQ(xa: Transactor[Eff])(e: String): Eff[Unit] = {
    sql"insert or ignore into command(command_string) values ($e)".update.run
      .transact(xa)
      .map(ignore)

  }

  def persistDirQ(xa: Transactor[Eff])(e: String): Eff[Unit] = {
    sql"insert or ignore into directory(dir) values ($e)".update.run
      .transact(xa)
      .map(ignore)

  }

  def persistArgsQ(
      xa: Transactor[Eff]
  )(commandString: String, args: List[String]): Eff[Unit] = {
    if (args.isEmpty) {
      ZIO.succeed(())
    } else {
      val values =
        args.map(arg ⇒ sql"($commandString, $arg)").intercalate(sql",")
      (sql"insert or ignore into command_arg(command_string, arg) values " ++ values).update.run
        .transact(xa)
        .map(ignore)
    }
  }

  def incrementCommandUseQ(xa: Transactor[Eff])(
      commandString: String
  ): Eff[Unit] = {
    sql"update command set uses = uses + 1 where command_string = $commandString".update.run
      .transact(xa)
      .map(ignore)

  }

  def incrementStoredCommandUseQ(xa: Transactor[Eff])(
      name: String
  ): Eff[Unit] = {
    sql"update stored_command set uses = uses + 1 where name= $name".update.run
      .transact(xa)
      .map(ignore)

  }

  def incrementDirUseQ(xa: Transactor[Eff])(
      dir: String
  ): Eff[Unit] = {
    sql"update directory set uses = uses + 1 where dir = $dir".update.run
      .transact(xa)
      .map(ignore)
  }

  def incrementArgsUseQ(xa: Transactor[Eff])(
      commandString: String,
      args: List[String]
  ): Eff[Unit] = {
    val inArgs            = args.toNel.map(a ⇒ Fragments.in(fr"arg", a))
    val withCommandString = sql"command_string = $commandString".some
    val query =
      sql"update command_arg set uses = uses + 1 " ++ whereAndOpt(
        inArgs,
        withCommandString
      )
    query.update.run
      .transact(xa)
      .map(ignore)
  }

  def persistCommandExecutionQ(
      xa: Transactor[Eff]
  )(e: CommandExecutionE) = {
    sql"""insert into command_execution (time, command_string, args, dir) 
          VALUES (${e.time}, ${e.commandString}, ${e.args}, ${e.dir});
       """.update.run
      .transact(xa)
      .map(ignore)
  }

  def saveStoredCommandQ(
      xa: Transactor[Eff]
  )(e: StoredCommandE) = {
    sql"""insert into stored_command(name, command_string, args, dir, uses)
          values(${e.name}, ${e.commandString}, ${e.args}, ${e.dir}, ${e.uses})
       """.update.run
      .transact(xa)
      .map(ignore)
  }

  def getCommandQ(xa: Transactor[Eff])(id: String) =
    sql"select name, command_string, args, dir, uses from stored_command where name = $id"
      .query[StoredCommandE]
      .option
      .transact(xa)

  def getTopCommandsQ(xa: Transactor[Eff]) =
    sql"select command_string, uses from command"
      .query[CommandE]
      .to[List]
      .transact(xa)

  def getTopDirectoriesQ(xa: Transactor[Eff]) =
    sql"select dir, uses from directory"
      .query[DirectoryE]
      .to[List]
      .transact(xa)

  def getTopArgsForCommandQ(xa: Transactor[Eff])(command: String) =
    sql"select arg from command_arg where command_string = $command"
      .query[String]
      .to[List]
      .transact(xa)

  def getStoredCommandsQ(xa: Transactor[Eff]) = 
    sql"select name, command_string, args, dir, uses from stored_command"
     .query[StoredCommandE]
      .to[List]
      .transact(xa)

  def getRecentCommandsQ(xa: Transactor[Eff]) = 
    sql"select time, command_string, args, dir from command_execution limit 1000"
    .query[CommandExecutionE]
    .to[List]
    .transact(xa)
}
