package com.mksoft.ctool

import doobie.{ConnectionIO, Transactor, _}
import zio.{Task, ZIO}
import doobie.implicits._
import cats.implicits._
import cats._
import cats.free.Free
import zio.interop.catz._
import com.mksoft.ctool.Model._
import com.mksoft.ctool.Utils.ignore
import zio.console._

object Repository {
  val migrations = List(
    sql"""
         create table commandExecution
      (
      execution_name varchar(255)  null,
      dir            varchar(1000) not null,
      command        varchar(1000) not null,
      args           varchar(1000) not null
      )
       """,
    sql"CREATE TABLE kekd1  (version int NOT NULL);",
    sql"CREATE TABLE kek12  (version int NOT NULL);"
  ).mapWithIndex(Tuple2.apply)

  def xa() =
    Transactor.fromDriverManager[Eff](
      "org.sqlite.JDBC",
      "jdbc:sqlite:sample.db",
      "",
      ""
    )
  def migrate(xa: Transactor[Eff]) = {
    for {
      currentSchemaVersion <- getCurrentVersionOrCreateTable.transact(xa)
      migrationsToRun = migrations.slice(currentSchemaVersion + 1, migrations.length)
      _ ←
        if (migrationsToRun.isEmpty) putStrLn("No migrations to run")
        else putStrLn(s"Migrating db from version $currentSchemaVersion ")
      _ ← ZIO.foreach_(migrationsToRun) {
        (migWithId) =>
          {
            val (mig, idx) = migWithId
            val migration = for {
              _ <-
                if (currentSchemaVersion < idx)
                  setVersionQ(idx)
                    .flatMap(_ ⇒ mig.update.run)
                    .flatMap(_ ⇒ ().pure[ConnectionIO])
                else ().pure[ConnectionIO]
            } yield mig
            migration.transact(xa) *> zio.console.putStrLn(
              s"migration run ${mig}"
            )
          }
      }
    } yield ()
  }
  val getCurrentVersionOrCreateTable: ConnectionIO[Int] =
    currentVersionQ
      .recoverWith {
        case _ =>
          createSchemaVersionTableQ
            .flatMap(_ => setInitialVersionQ(-1)).as(Some(-1))
      }
      .flatMap {
        case Some(ver) ⇒ ver.pure[ConnectionIO]
        case None ⇒ setVersionQ(-1).as(-1)
      }

  val createSchemaVersionTableQ =
    sql"CREATE TABLE if not exists  schema_version (version int NOT NULL)".update.run
      .map(ignore)

  def setInitialVersionQ(v: Int) =
    sql"INSERT INTO schema_version (version) VALUES ($v)".update.run

  def setVersionQ(v: Int) =
    sql"UPDATE schema_version SET version = $v".update.run

  def currentVersionQ: doobie.ConnectionIO[Option[Int]] =
    sql"SELECT version FROM schema_version".query[Int].option
import cats._, cats.data._, cats.implicits._
  import doobie._, doobie.implicits._
  def getCommandQ(xa: Transactor[Eff])(id: String) =
    sql"select * from exec".query[String].option.transact(xa)


}
