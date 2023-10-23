package dapex.dbservice.domain.db.repository

import cats.Applicative
import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import dapex.dbservice.domain.db.repository.CustomerRepository._
import dapex.dbservice.domain.db.repository.entities.Customer
import dapex.dbservice.entities.ServiceError
import dapex.dbservice.entities.ServiceError.DatabaseInsertionError
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger

class CustomerRepository[F[_]: Applicative: MonadCancelThrow: Logger](xa: Transactor[F])
    extends CustomerRepositoryAlgebra[F] {

  override def getCustomerById(id: Long): F[Option[Customer]] =
    getCustomerByIdSql(id).option
      .transact(xa)

  override def getCustomerByEmail(email: String): F[Option[Customer]] =
    getCustomerByEmailSql(email).option
      .transact(xa)

  override def updateCustomer(id: Long, updatedCustomer: Customer): F[Int] =
    updateCustomerSql(id, updatedCustomer).run
      .transact(xa)

  override def insertCustomer(customer: Customer): F[Either[ServiceError, Long]] =
    insertCustomerSql(customer)
      .withUniqueGeneratedKeys[Long]("id")
      .transact(xa)
      .redeem(ex => Left(DatabaseInsertionError(ex.getMessage)), id => Right(id))

  override def deleteCustomerById(id: Long): F[Int] =
    deleteCustomerByIdSql(id).run
      .transact(xa)

  override def deleteCustomerByEmail(email: String): F[Int] =
    deleteCustomerByEmailSql(email).run
      .transact(xa)
}

private object CustomerRepository {

  def getCustomerByIdSql(id: Long): doobie.Query0[Customer] =
    sql"""
          SELECT
                id,
                forename,
                surname,
                email
              FROM customer
              WHERE id = $id
         """
      .queryWithLabel[Customer]("getCustomerByIdSql")

  def getCustomerByEmailSql(email: String): doobie.Query0[Customer] =
    sql"""
          SELECT
            id,
            forename,
            surname,
            email
          FROM customer
          WHERE email = $email
         """
      .queryWithLabel[Customer]("getCustomerByEmailSql")

  def updateCustomerSql(id: Long, update: Customer): doobie.Update0 =
    sql"""
          UPDATE customer
            SET
              forename = ${update.forename},
              surname = ${update.surname},
              email = ${update.email}
          WHERE id = $id
         """.updateWithLabel("updateCustomerSql")

  def insertCustomerSql(c: Customer): doobie.Update0 =
    sql"""
          INSERT INTO customer (forename, surname, email)
          VALUES
            (${c.forename}, ${c.surname}, ${c.email})
         """
      .updateWithLabel("insertCustomerSql")

  def deleteCustomerByIdSql(id: Long): doobie.Update0 =
    sql"""
          DELETE FROM customer
          WHERE id = $id
         """.updateWithLabel("deleteCustomerByIdSql")

  def deleteCustomerByEmailSql(email: String): doobie.Update0 =
    sql"""
          DELETE FROM customer
          WHERE email = $email
         """.updateWithLabel("deleteCustomerByEmailSql")
}
