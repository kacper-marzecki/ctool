package com.mksoft.ctool

import doobie.{ConnectionIO, Transactor, _}
import zio.{Task, ZIO}
import doobie.implicits._
import cats.implicits._
import cats._
import cats.free.Free
import zio.interop.catz._
import com.mksoft.ctool.Model._
import zio.console._

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
      sql"CREATE TABLE IF NOT EXISTS kek  (version int NOT NULL);",
      sql"CREATE TABLE kek1  (version int NOT NULL);",
      sql"CREATE TABLE kek12  (version int NOT NULL);"
    )
      .mapWithIndex((migration, index) => (migration, index))
    for {
      currentSchemaVersion <- getCurrentVersionOrCreateTable.transact(xa)
      migrationsToRun = a.slice(currentSchemaVersion, a.length - 1)
      _ ←
        if (migrationsToRun.isEmpty) putStrLn("No migrations to run")
        else putStrLn(s"Migrating db from version $currentSchemaVersion ")
      _ ← ZIO.foreach_(a.slice(currentSchemaVersion + 1, a.length)) {
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
            .flatMap(_ => setInitialVersionQ(-1))
            .as(Some(-1))
      }
      .flatMap {
        case Some(ver) ⇒ Free.pure(ver)
        case None ⇒ setVersionQ(-1).as(-1)
      }

  val createSchemaVersionTableQ =
    sql"CREATE TABLE if not exists  schema_version (version int NOT NULL)".update.run
      .map(_ => ())

  def setInitialVersionQ(v: Int) =
    sql"INSERT INTO schema_version (version) VALUES ($v)".update.run

  def setVersionQ(v: Int) =
    sql"UPDATE schema_version SET version = $v".update.run

  def currentVersionQ: doobie.ConnectionIO[Option[Int]] =
    sql"SELECT version FROM schema_version".query[Int].option

  def getCommandQ(xa: Transactor[Eff])(id: String) =
    sql"select * from asd".query[String].to[List].transact(xa)

}
