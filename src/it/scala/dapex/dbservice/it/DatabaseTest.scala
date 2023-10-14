package dapex.dbservice.it

import cats.effect.IO
import dapex.config.{DatabaseConfig, ServerConfiguration}
import dapex.dbservice.domain.db.migration.FlywayDatabaseMigrator
import dapex.dbservice.entities.MysqlConfig
import io.circe
import io.circe.config.parser
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import org.typelevel.log4cats.slf4j.Slf4jLogger

class DatabaseTest extends AnyFlatSpec with Matchers with ScalaFutures  {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(30, Seconds), interval = Span(100, Millis))

  import cats.effect.unsafe.implicits.global

  private implicit def unsafeLogger = Slf4jLogger.getLogger[IO]

  val container = setupMySQLContainer()

  val conf = parser.decodePath[DatabaseConfig]("db").toOption
  val dbConfig = MysqlConfig.fromOption(conf)

  it should "start the mysql container for further tests" in {
    val username = container.getUsername
    val password = container.getPassword
    val url = container.getJdbcUrl

    username shouldBe "mysql"
    password shouldBe "mysqlpassword"
    url shouldBe "jdbc:mysql://localhost:3306/test"
  }

  it should "create the tables" in {
    val dbMigrator = new FlywayDatabaseMigrator(dbConfig)
    val result = dbMigrator.migrateDatabase()
    result.isDefined shouldBe true
  }

  private def setupMySQLContainer(): MySQLContainer[Nothing] = {
    val container = new MySQLContainer("mysql:8.1.0") {
       def addFixedPort(hostPort: Int, containerPort: Int): Unit =
        super.addFixedExposedPort(hostPort, containerPort)
    }
    container.withUsername("mysql")
    container.withPassword("mysqlpassword")
    container.addFixedPort(3306, 3306)
    container.start()
    container
  }

}
