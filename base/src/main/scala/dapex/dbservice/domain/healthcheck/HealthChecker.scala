package dapex.dbservice.domain.healthcheck

import dapex.dbservice.domain.healthcheck.entities.HealthCheckerResponse

trait HealthChecker[F[_]] {

  val name: String

  def checkHealth(): F[HealthCheckerResponse]

}
