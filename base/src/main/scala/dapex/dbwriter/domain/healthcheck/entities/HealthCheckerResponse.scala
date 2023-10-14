package dapex.dbwriter.domain.healthcheck.entities

case class HealthCheckerResponse(
    name: String,
    status: HealthStatus
)
