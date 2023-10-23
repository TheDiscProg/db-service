package dapex.dbservice.domain.db.repository

import dapex.dbservice.domain.db.repository.entities.Customer
import dapex.dbservice.entities.ServiceError

trait CustomerRepositoryAlgebra[F[_]] {

  def getCustomerById(id: Long): F[Option[Customer]]

  def getCustomerByEmail(email: String): F[Option[Customer]]

  def updateCustomer(id: Long, updatedCustomer: Customer): F[Int]

  def insertCustomer(customer: Customer): F[Either[ServiceError, Long]]

  def deleteCustomerById(id: Long): F[Int]

  def deleteCustomerByEmail(email: String): F[Int]

}
