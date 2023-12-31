package dapex

import cats.data.NonEmptyList
import cats.effect.{Async, Resource}
import cats.{Monad, MonadError, Parallel}
import com.comcast.ip4s._
import dapex.config.ServerConfiguration
import dapex.dbservice.domain.db.connector.DbTransactor
import dapex.dbservice.domain.db.migration.FlywayDatabaseMigrator
import dapex.dbservice.domain.db.repository.{CustomerRepository, UserRepository}
import dapex.dbservice.domain.healthcheck.{
  HealthCheckService,
  HealthChecker,
  HealthcheckAPIHandler,
  SelfHealthCheck
}
import dapex.dbservice.entities.{AppService, MysqlConfig}
import io.circe.config.parser
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.{Logger => Log4CatsLogger}
import dapex.guardrail.healthcheck.HealthcheckResource

object AppServer {

  def createServer[F[
      _
  ]: Monad: Async: Log4CatsLogger: Parallel: MonadError[*[_], Throwable]]()
      : Resource[F, AppService] =
    for {
      conf <- Resource.eval(parser.decodePathF[F, ServerConfiguration](path = "server"))

      // Health checkers
      checkers = NonEmptyList.of[HealthChecker[F]](SelfHealthCheck[F])
      healthCheckers = HealthCheckService(checkers)
      healthRoutes = new HealthcheckResource().routes(
        new HealthcheckAPIHandler[F](healthCheckers)
      )

      // Database migration
      dbConfig = MysqlConfig.fromOption(conf.db)
      dbMigrator = new FlywayDatabaseMigrator(dbConfig)
      _ = dbMigrator.migrateDatabase()
      xa <- DbTransactor.getDatabaseTransactor(dbConfig)
      // Database repositories
      customerRepository = new CustomerRepository[F](xa)
      userRepository = new UserRepository[F](xa)

      // Routes and HTTP App
      allRoutes = healthRoutes.orNotFound
      httpApp = Logger.httpApp(logHeaders = true, logBody = true)(allRoutes)

      // Build HTTP Server
      httpPort = Port.fromInt(conf.http.port.value)
      httpHost = Ipv4Address.fromString(conf.http.host.value)
      server <- EmberServerBuilder.default
        .withPort(httpPort.getOrElse(port"8000"))
        .withHost(httpHost.getOrElse(ipv4"0.0.0.0"))
        .withHttpApp(httpApp)
        .build
    } yield AppService(server)
}
