package dapex.dbservice.domain.healthcheck

import dapex.dbservice.domain.healthcheck.entities.HealthCheckStatus

trait HealthCheckAlgebra[F[_]] {

  def checkHealth: F[HealthCheckStatus]
}
