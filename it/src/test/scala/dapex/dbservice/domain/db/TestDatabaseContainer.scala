package dapex.dbservice.domain.db

import cats.effect.IO
import dapex.config.DatabaseConfig
import dapex.dbservice.domain.db.connector.DbTransactor
import dapex.dbservice.domain.db.migration.FlywayDatabaseMigrator
import dapex.dbservice.entities.MysqlConfig
import io.circe.config.parser
import org.flywaydb.core.api.output.MigrateResult
import org.testcontainers.containers.MySQLContainer
import org.typelevel.log4cats.slf4j.Slf4jLogger

object TestDatabaseContainer {
  implicit def logger = Slf4jLogger.getLogger[IO]

  private val db = setupMySQLContainer()

  val dbConfig = db._1
  val container = db._2

  val transactorResource = DbTransactor.getDatabaseTransactor(dbConfig)

  def migrate = {
    val dbMigrator = new FlywayDatabaseMigrator(dbConfig)
    val result: Option[MigrateResult] = dbMigrator.migrateDatabase()
    if (!result.isDefined || !result.get.success)
      throw new RuntimeException("Could not start MySQL database test container for tests!")
  }

  private def setupMySQLContainer() = {
    val conf = parser.decodePath[DatabaseConfig]("db").toOption
    val dbConfig = MysqlConfig.fromOption(conf)

    val container = new MySQLContainer("mysql:8.1.0") {
      def addFixedPort(hostPort: Int, containerPort: Int): Unit =
        super.addFixedExposedPort(hostPort, containerPort)
    }
    container.addFixedPort(3306, 3306)
    container.withDatabaseName("shareprice")
    (dbConfig, container)
  }

}
