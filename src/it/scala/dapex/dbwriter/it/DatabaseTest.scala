package dapex.dbwriter.it

import cats.effect.IO
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

  it should "start the mysql container for further tests" in {
    val username = container.getUsername
    val password = container.getPassword
    val url = container.getJdbcUrl

    username shouldBe "mysql"
    password shouldBe "mysqlpassword"
    url shouldBe "jdbc:mysql://localhost:3306/test"
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
