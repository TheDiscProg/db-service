package dapex.dbwriter.domain.healthcheck

import dapex.dbwriter.domain.healthcheck.entities.HealthCheckStatus

trait HealthCheckAlgebra[F[_]] {

  def checkHealth: F[HealthCheckStatus]
}
