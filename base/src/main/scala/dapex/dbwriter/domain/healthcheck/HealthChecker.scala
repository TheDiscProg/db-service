package dapex.dbwriter.domain.healthcheck

import dapex.dbwriter.domain.healthcheck.entities.HealthCheckerResponse

trait HealthChecker[F[_]] {

  val name: String

  def checkHealth(): F[HealthCheckerResponse]

}
