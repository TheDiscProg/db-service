package dapex.dbservice.domain.db.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dapex.dbservice.domain.db.TestDatabaseContainer
import dapex.dbservice.domain.db.repository.entities.Customer
import dapex.dbservice.entities.ServiceError
import dapex.dbservice.entities.ServiceError.DatabaseInsertionError
import dapex.dbservice.fixture.{DefaultFutureSetting, TestFixtures}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.Future

class CustomerRepositoryTest
    extends AnyFlatSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with EitherValues
    with BeforeAndAfterAll
    with DefaultFutureSetting
    with TestFixtures {

  implicit def logger = Slf4jLogger.getLogger[IO]

  val config = TestDatabaseContainer.dbConfig
  val container = TestDatabaseContainer.container

  val xaResource = TestDatabaseContainer.transactorResource

  override def beforeAll(): Unit = {
    container.start()
    TestDatabaseContainer.migrate
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    container.stop()
    super.afterAll()
  }

  it should "insert customers and get rows by id and email" in {
    val result = xaResource
      .use { xa =>
        val sut = new CustomerRepository[IO](xa)
        for {
          jsIdEither: Either[ServiceError, Long] <- sut.insertCustomer(johnS)
          tsIdEither: Either[ServiceError, Long] <- sut.insertCustomer(tinaS)
          jsId = jsIdEither.value
          tsId = tsIdEither.value
          js <- sut.getCustomerById(jsId)
          ts <- sut.getCustomerByEmail(tinaS.email)
        } yield (js, ts)
      }
      .unsafeToFuture()

    whenReady(result) { tuple =>
      tuple._1.isDefined shouldBe true
      tuple._2.isDefined shouldBe true
      val js = tuple._1.value
      val ts = tuple._2.value
      js.email shouldBe johnS.email
      ts.email shouldBe tinaS.email
    }
  }

  it should "not insert customer when email already exists" in {
    val result: Future[Either[ServiceError, Long]] = xaResource
      .use { xa =>
        val sut = new CustomerRepository[IO](xa)
        sut.insertCustomer(tinaS)
      }
      .unsafeToFuture()

    whenReady(result) { either =>
      either.isLeft shouldBe true
      either.left.value shouldBe DatabaseInsertionError(
        "Duplicate entry 'tinas@test.com' for key 'customer.email'"
      )
    }
  }
}
