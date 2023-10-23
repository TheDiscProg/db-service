package dapex.dbservice.domain.db.repository.entities

case class User(
    id: Long,
    customerId: Long,
    username: String,
    password: String,
    enabled: Boolean
)
