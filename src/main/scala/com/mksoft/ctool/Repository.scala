package com.mksoft.ctool

import java.sql.Timestamp

import cats.implicits._
import com.mksoft.ctool.Model._
import com.mksoft.ctool.Utils.ignore
import doobie.implicits._
import doobie.{ConnectionIO, Read, Transactor}
import zio.console._
import zio.interop.catz._
import zio.{UIO, ZIO}

import scala.io.{BufferedSource, Source}

object Repository {
  object implicits {
    implicit val storedCommandPut: Read[StoredCommandE] =
      Read[(String, String, String, String, Int)].map {
        case (a, b, c, d, e) ⇒ StoredCommandE(a, b, c, d, e)
      }
  }
  import implicits._
  def xa() =
    Transactor.fromDriverManager[Eff](
      "org.sqlite.JDBC",
      "jdbc:sqlite:sample.db",
      "",
      ""
    )
  import cats._, cats.data._, cats.implicits._
  import doobie._, doobie.implicits._

  def getCommandQ(xa: Transactor[Eff])(id: String) =
    sql"select name, command_string, args, dir, uses from stored_command where name = $id"
      .query[StoredCommandE]
      .option
      .transact(xa)
}
