package com.mksoft.ctool.repository

import com.mksoft.ctool.Model.Eff
import com.mksoft.ctool.Utils.ignore
import doobie.util.transactor.Transactor
import zio.console.putStrLn
import scala.io.{BufferedSource, Source}
import zio._, ZIO._, zio.interop.catz._
import doobie._, doobie.implicits._
import zio._, ZIO._
import cats.data._, cats.implicits._

object RepositorySetup {
  def readMigrations = {
    val read           = ZIO(Source.fromResource("migrations.sql"))
    val close          = (s: BufferedSource) ⇒ ZIO.succeed(s.close())
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
      currentSchemaVersion ← getCurrentVersionOrCreateTable.transact(xa)
      migrations           ← readMigrations
      migrationsToRun =
        migrations.slice(currentSchemaVersion + 1, migrations.length)
      _ ←
        if (migrationsToRun.isEmpty) putStrLn("No migrations to run")
        else putStrLn(s"Migrating db from version $currentSchemaVersion")
      _ ← foreach_(migrationsToRun.mapWithIndex(Tuple2.apply)) { migWithId =>
        {
          val (mig, idx) = migWithId
          val migration = for {
            _ <-
              if (currentSchemaVersion < idx)
                setVersionQ(idx)
                  .flatMap(_ ⇒ Fragment.const(mig).update.run)
                  .flatMap(_ ⇒ ().pure[ConnectionIO])
              else ().pure[ConnectionIO]
          } yield mig
          migration.transact(xa) *> zio.console.putStrLn(
            s"Migration run ${mig}"
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
        case None      ⇒ setVersionQ(-1).as(-1)
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

}
