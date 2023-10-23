package dapex.dbservice.domain.db.repository

import dapex.dbservice.domain.db.repository.entities.User
import dapex.dbservice.entities.ServiceError

trait UserRepositoryAlgebra[F[_]] {

  def getUserById(id: Long): F[Option[User]]

  def getUserByUsername(username: String): F[Option[User]]

  def updateUser(id: Long, updatedUser: User): F[Int]

  def insertUser(user: User): F[Either[ServiceError, Long]]

  def disableUserById(id: Long): F[Int]

  def disableUserByUsername(username: String): F[Int]

  def enableUserById(id: Long): F[Int]

  def enableUserByUsername(username: String): F[Int]
}
