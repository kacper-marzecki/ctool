package com.mksoft.ctool

import doobie.{ConnectionIO, Transactor, _}
import zio.{Task, UIO, URIO, ZIO}
import doobie.implicits._
import cats.implicits._
import cats._
import cats.free.Free
import zio.interop.catz._
import com.mksoft.ctool.Model._
import com.mksoft.ctool.Utils.ignore
import zio.console._
import zio.interop.catz._

import scala.collection.mutable.ListBuffer
import scala.io.{BufferedSource, Source}

object Repository {
//  val migrations = List(
//    sql"""
//         create table commandExecution
//      (
//      time timestamp not null,
//      commandString            varchar(1000) not null,
//      args        varchar(1000) not null,
//      dir           varchar(1000) not null
//      );
//       """,
//    sql"""
//          CREATE TABLE storedCommand  (
//          name varchar(1000) not null,
//          args varchar(1000) not null,
//          dir varchar(1000) not null,
//          uses int not null default 0
//         );
//         """,
//    sql"CREATE TABLE kek12  (version int NOT NULL);"
//  ).mapWithIndex(Tuple2.apply)

  def xa() =
    Transactor.fromDriverManager[Eff](
      "org.sqlite.JDBC",
      "jdbc:sqlite:sample.db",
      "",
      ""
    )

  def readMigrations = {
    val read = ZIO(Source.fromResource("migrations.sql"))
    val close = (s: BufferedSource) ⇒ ZIO.succeed(s.close())
    val migrationsFile = ZIO.bracket(read)(close)
    migrationsFile(file ⇒ { UIO(parseMigrations(file.getLines().toList)) })
  }

  def parseMigrations(lines: List[String]): List[String] = {
    val migrationEndIdx = lines.indexOf("--migration_end")
    migrationEndIdx match {
      case -1 ⇒ Nil
      case x ⇒ {
        val (migration, rest) = lines.splitAt(x + 1)
        migration.intercalate("\n") :: parseMigrations(rest)
      }
    }
  }

  def migrate(xa: Transactor[Eff]) = {
    for {
      currentSchemaVersion <- getCurrentVersionOrCreateTable.transact(xa)
      migrations ← readMigrations
      migrationsToRun =
        migrations.slice(currentSchemaVersion + 1, migrations.length)
      _ ←
        if (migrationsToRun.isEmpty) putStrLn("No migrations to run")
        else putStrLn(s"Migrating db from version $currentSchemaVersion ")
      _ ← ZIO.foreach_(migrationsToRun.mapWithIndex(Tuple2.apply)) {
        (migWithId) =>
          {
            val (mig, idx) = migWithId
            val migration = for {
              _ <-
                if (currentSchemaVersion < idx)
                  setVersionQ(idx)
                    .flatMap(_ ⇒ doobie.Fragment.const(mig).update.run)
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
