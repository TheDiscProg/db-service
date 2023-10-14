package dapex.dbservice.domain.healthcheck.entities

case class HealthCheckerResponse(
    name: String,
    status: HealthStatus
)
