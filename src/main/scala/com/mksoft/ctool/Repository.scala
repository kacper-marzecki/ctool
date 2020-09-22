package com.mksoft.ctool

import doobie.Transactor
import zio.Task
import doobie.implicits._
import cats.implicits._
import doobie._
import zio.interop.catz._
import com.mksoft.ctool.Model._

object Repository {

  def xa() = Transactor.fromDriverManager[Eff](
    "org.sqlite.JDBC", "jdbc:sqlite:sample.db", "", ""
  )

  def createSchemaVersionTableQ(xa: Transactor[Eff]) =
    sql"CREATE TABLE schema_version (version int NOT NULL)".update.run.transact(xa)

  def setInitialVersionQ( xa: Transactor[Eff])(v: Int) =
    sql"INSERT INTO schema_version (version) VALUES ($v)".update.run.transact(xa)

  def setVersionQ( xa: Transactor[Eff])(v: Int) =
    sql"UPDATE schema_version SET version = $v".update.run.transact(xa)

  def currentVersionQ( xa: Transactor[Eff]) =
    sql"SELECT version FROM schema_version".query[Int].option.transact(xa)

  def getCommandQ(xa: Transactor[Eff])(id: String) =
    sql"select * from asd".query[String].to[List].transact(xa)

}
