package dapex.dbservice.domain.db

import org.scalatest.{BeforeAndAfterAll, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TestDatabaseContainerSpec
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with BeforeAndAfterAll {

  val config = TestDatabaseContainer.dbConfig
  val container = TestDatabaseContainer.container

  override def beforeAll(): Unit = {
    container.start()
    TestDatabaseContainer.migrate
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    container.stop()
    super.afterAll()
  }
  it should "start the mysql container for further tests" in {

    val username = container.getUsername
    val password = container.getPassword
    val url = container.getJdbcUrl

    username shouldBe "test"
    password shouldBe "test"
    url shouldBe config.dbConfig.url
  }
}
