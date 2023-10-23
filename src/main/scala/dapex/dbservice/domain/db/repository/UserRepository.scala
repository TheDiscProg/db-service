package dapex.dbservice.domain.db.repository

import cats.Applicative
import cats.effect.kernel.MonadCancelThrow
import cats.implicits.catsSyntaxApplicativeError
import dapex.dbservice.domain.db.repository.UserRepository.{
  getUserByIdSql,
  getUserByUsernameSQL,
  insertUserSql,
  setEnabledFlagByIdSql,
  setEnabledFlagByUsernameSql,
  updateUserSql
}
import dapex.dbservice.domain.db.repository.entities.User
import dapex.dbservice.entities.ServiceError
import dapex.dbservice.entities.ServiceError.DatabaseInsertionError
import doobie.util.transactor.Transactor
import doobie.implicits._
import org.typelevel.log4cats.Logger

class UserRepository[F[_]: Applicative: MonadCancelThrow: Logger](xa: Transactor[F])
    extends UserRepositoryAlgebra[F] {

  override def getUserById(id: Long): F[Option[User]] =
    getUserByIdSql(id).option.transact(xa)

  override def getUserByUsername(username: String): F[Option[User]] =
    getUserByUsernameSQL(username).option.transact(xa)

  override def updateUser(id: Long, updatedUser: User): F[Int] =
    updateUserSql(id, updatedUser).run.transact(xa)

  override def insertUser(user: User): F[Either[ServiceError, Long]] =
    insertUserSql(user)
      .withUniqueGeneratedKeys[Long]("id")
      .transact(xa)
      .redeem(ex => Left(DatabaseInsertionError(ex.getMessage)), id => Right(id))

  override def disableUserById(id: Long): F[Int] =
    setEnabledFlagByIdSql(id, flag = false).run
      .transact(xa)

  override def disableUserByUsername(username: String): F[Int] =
    setEnabledFlagByUsernameSql(username, flag = false).run
      .transact(xa)

  override def enableUserById(id: Long): F[Int] =
    setEnabledFlagByIdSql(id, flag = true).run
      .transact(xa)

  override def enableUserByUsername(username: String): F[Int] =
    setEnabledFlagByUsernameSql(username, flag = true).run
      .transact(xa)
}

private object UserRepository {

  def getUserByIdSql(id: Long): doobie.Query0[User] =
    sql"""
          SELECT
            id,
            customer_id,
            username,
            password,
            enabled
          FROM shareprice_user
          WHERE id = $id
         """.queryWithLabel[User]("getUserByIdSQL")

  def getUserByUsernameSQL(username: String): doobie.Query0[User] =
    sql"""
          SELECT
            id,
            customer_id,
            username,
            password,
            enabled
          FROM shareprice_user
          WHERE username = $username
         """.queryWithLabel[User]("getUserByUsernameSQL")

  def updateUserSql(id: Long, user: User): doobie.Update0 =
    sql"""
          UPDATE shareprice_user
          SET
            username = ${user.username},
            password = ${user.password},
            enabled = ${user.enabled}
          WHERE id = $id
         """.updateWithLabel("updateUserSql")

  def insertUserSql(user: User): doobie.Update0 =
    sql"""
          INSERT INTO shareprice_user(customer_id, username, password, enabled)
          VALUES
            (${user.customerId}, ${user.username}, ${user.password}, ${user.enabled})
         """.updateWithLabel("insertUserSql")

  def setEnabledFlagByIdSql(id: Long, flag: Boolean): doobie.Update0 =
    sql"""
          UPDATE shareprice_user
          SET
            enabled = $flag
          WHERE id = $id
         """.updateWithLabel("setEnabledFlagByIdSql")

  def setEnabledFlagByUsernameSql(username: String, flag: Boolean): doobie.Update0 =
    sql"""
          UPDATE shareprice_user
          SET
            enabled = $flag
          WHERE username = $username
         """.updateWithLabel("setEnabledFlagByUsernameSql")
}
