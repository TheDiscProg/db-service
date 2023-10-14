package dapex.dbwriter.domain.db.migration

import com.typesafe.scalalogging.LazyLogging
import dapex.dbwriter.entities.MysqlConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationInfoService

class FlywayDatabaseMigrator(dbConfig: MysqlConfig) extends LazyLogging {

  private lazy val sql = dbConfig.dbConfig

  private lazy val fly = {
    logger.info(s"FlywayDatabaseMigrator: Creating Flyway for database migration")
    Flyway.configure().dataSource(sql.url, sql.user, sql.password).load()
  }

  def migrateDatabase() = {
    val info = fly.info().pending()
    if (info.size != 0) {
      val pending = info.head
      logger.info(s"FlywayDatabaseMigrator: Migrating database to ${pending.getVersion}")
      val migrationResult = fly.migrate()
      logger.info(s"FlywayDatabaseMigrator: Migration result: $migrationResult")
    } else {
      logger.info(s"FlywayDatabaseMigrator: Not applying migration scripts")
    }
  }
}
