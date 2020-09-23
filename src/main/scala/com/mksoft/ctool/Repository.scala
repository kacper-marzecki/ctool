package com.mksoft.ctool

import doobie.{ConnectionIO, Transactor, _}
import zio.Task
import doobie.implicits._
import cats.implicits._
import cats._
import zio.interop.catz._
import com.mksoft.ctool.Model._

object Repository {

  def xa() =
    Transactor.fromDriverManager[Eff](
      "org.sqlite.JDBC",
      "jdbc:sqlite:sample.db",
      "",
      ""
    )
  def migrate(xa: Transactor[Eff]) = {
    val a = List(
      sql"CREATE TABLE IF NOT EXISTS main.kek  (version int NOT NULL);"
    )
      .mapWithIndex((migration, index) => (migration, index))
    zio.ZIO.foreach(a)((migWithId) => {
      val (mig, idx) = migWithId
      val updateSchemaVersion = setVersionQ(idx)
      val migration = for {
        currentSchemaVersion <- getCurrentVersionOrCreateTable
        _ <-
          if (currentSchemaVersion <= idx) updateSchemaVersion
          else doobie.Fragment.empty.update.run
//        _ <- currentSchemaVersion match {
//          case Some(version) =>
//            if (version <= idx) updateSchemaVersion
//            else doobie.Fragment.empty.update.run
//          case _ => createSchemaVersionTableQ *>
//        }
      } yield mig
      migration.transact(xa) *> zio.console.putStrLn("migration run")
    })
  }
  val getCurrentVersionOrCreateTable: ConnectionIO[Int] =
    for {
      version <- currentVersionQ
      .recoverWith {
        case _ =>
          createSchemaVersionTableQ
            .flatMap(_ => setVersionQ(-1))
            .as(Some(-1))
      }
      _ <- version match {
        case Some(v) => v
        case _ => setVersionQ(-1).as(-1)
    }
    }
//      .onError{
//      case _ => createSchemaVersionTableQ
//    }.recoverWith{case _ => currentVersionQ}

  val createSchemaVersionTableQ =
    sql"CREATE TABLE if not exists  schema_version (version int NOT NULL)".update.run
      .map(_ => ())

  def setInitialVersionQ(xa: Transactor[Eff])(v: Int) =
    sql"INSERT INTO schema_version (version) VALUES ($v)".update.run
      .transact(xa)

  def setVersionQ(v: Int) =
    sql"UPDATE schema_version SET version = $v".update.run

  def currentVersionQ: doobie.ConnectionIO[Option[Int]] =
    sql"SELECT version FROM schema_version".query[Int].option

  def getCommandQ(xa: Transactor[Eff])(id: String) =
    sql"select * from asd".query[String].to[List].transact(xa)

}
