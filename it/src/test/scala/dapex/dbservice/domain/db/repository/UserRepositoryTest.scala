package dapex.dbservice.domain.db.repository

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dapex.dbservice.domain.db.TestDatabaseContainer
import dapex.dbservice.domain.db.repository.entities.User
import dapex.dbservice.entities.ServiceError
import dapex.dbservice.entities.ServiceError.DatabaseInsertionError
import dapex.dbservice.fixture.{DefaultFutureSetting, TestFixtures}
import org.scalatest.{BeforeAndAfterAll, EitherValues, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.slf4j.Slf4jLogger

class UserRepositoryTest
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

  val userJSId = 2L
  val updatedUsername = "js@test.com"

  override def beforeAll(): Unit = {
    container.start()
    TestDatabaseContainer.migrate
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    container.stop()
    super.afterAll()
  }

  it should "not insert user unless a customer exists" in {
    val result = xaResource
      .use { xa =>
        val sut = new UserRepository[IO](xa)
        sut.insertUser(userS)
      }
      .unsafeToFuture()

    whenReady(result) { either =>
      either.isLeft shouldBe true
      either.left.value shouldBe a[DatabaseInsertionError]
    }
  }

  it should "insert users after a customer" in {
    val result = xaResource
      .use { xa =>
        val sut = new UserRepository[IO](xa)
        val customerRepository = new CustomerRepository[IO](xa)
        for {
          cIdJD <- customerRepository.insertCustomer(johnS)
          cIdTS <- customerRepository.insertCustomer(tinaS)
          _ <- logger.info(s"[$cIdJD & $cIdTS]")
          userJsId <- sut.insertUser(userS.copy(customerId = cIdJD.value))
          userTsId <- sut.insertUser(userT.copy(customerId = cIdTS.value))
        } yield (userJsId, userTsId)
      }
      .unsafeToFuture()

    whenReady(result) { eithers =>
      val user1 = eithers._1
      val user2 = eithers._2
      user1.isRight shouldBe true
      user2.isRight shouldBe true
      user1.value shouldBe 2L
      user2.value shouldBe 3L
    }
  }

  it should "handle getting user by Id" in {
    val result = xaResource
      .use { xa =>
        val sut = new UserRepository[IO](xa)
        for {
          js <- sut.getUserById(userJSId)
          ts <- sut.getUserById(4L)
        } yield (js, ts)

      }
      .unsafeToFuture()

    whenReady(result) { options =>
      val js = options._1
      val ts = options._2
      js.isDefined shouldBe true
      js.value.username shouldBe userS.username
      ts.isDefined shouldBe false
    }
  }

  it should "get user by username" in {
    val result = xaResource
      .use { xa =>
        val sut = new UserRepository[IO](xa)
        for {
          js <- sut.getUserByUsername(userS.username)
          ts <- sut.getUserByUsername("another")
        } yield (js, ts)
      }
      .unsafeToFuture()

    whenReady(result) { options =>
      val js = options._1
      val ts = options._2
      js.isDefined shouldBe true
      js.value.username shouldBe userS.username
      ts.isDefined shouldBe false
    }
  }

  it should "update user" in {
    val result = xaResource
      .use { xa =>
        val sut = new UserRepository[IO](xa)
        sut.updateUser(userJSId, userS.copy(username = updatedUsername))
      }
      .unsafeToFuture()

    whenReady(result) { rows =>
      rows shouldBe 1
    }
  }

  it should "disable and enable by ID" in {
    val result = xaResource
      .use { xa =>
        val sut = new UserRepository[IO](xa)
        for {
          updatedRowsDisabled <- sut.disableUserById(userJSId)
          disabled <- sut.getUserById(userJSId)
          updatedRowsEnabled <- sut.enableUserById(userJSId)
          enabled <- sut.getUserById(userJSId)
        } yield (updatedRowsDisabled, disabled, updatedRowsEnabled, enabled)
      }
      .unsafeToFuture()

    whenReady(result) { r =>
      val numberUpdatedOnDisabled = r._1
      val disabledUser = r._2.value
      val numberUpdatedOnEnabled = r._3
      val enabledUser = r._4.value

      numberUpdatedOnDisabled shouldBe 1
      numberUpdatedOnEnabled shouldBe 1
      disabledUser.enabled shouldBe false
      enabledUser.enabled shouldBe true
    }
  }

  it should "disable and enable by username" in {
    val result = xaResource
      .use { xa =>
        val sut = new UserRepository[IO](xa)
        for {
          updatedRowsDisabled <- sut.disableUserByUsername(updatedUsername)
          disabled <- sut.getUserByUsername(updatedUsername)
          updatedRowsEnabled <- sut.enableUserByUsername(updatedUsername)
          enabled <- sut.getUserByUsername(updatedUsername)
        } yield (updatedRowsDisabled, disabled, updatedRowsEnabled, enabled)
      }
      .unsafeToFuture()

    whenReady(result) { r =>
      val numberUpdatedOnDisabled = r._1
      val disabledUser = r._2.value
      val numberUpdatedOnEnabled = r._3
      val enabledUser = r._4.value

      numberUpdatedOnDisabled shouldBe 1
      numberUpdatedOnEnabled shouldBe 1
      disabledUser.enabled shouldBe false
      enabledUser.enabled shouldBe true
    }
  }

}
