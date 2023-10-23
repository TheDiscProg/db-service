package dapex.dbservice.fixture

import dapex.dbservice.domain.db.repository.entities.{Customer, User}

trait TestFixtures {

  val johnS = Customer(0L, "John", "Smith", "john.smith@test.com")
  val tinaS = Customer(0L, "Tina", "Smith", "tinas@test.com")

  val userS = User(0L, 0L, "johns", "test123", true)
  val userT = User(0L, 0L, "tina", "password1234", true)
}
