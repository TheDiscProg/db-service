package dapex.dbservice.domain.db.connector

import cats.effect.Async
import cats.effect.kernel.Resource
import dapex.dbservice.entities.MysqlConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object DbTransactor {

  def getDatabaseTransactor[F[_]: Async](
      dbcfg: MysqlConfig
  ): Resource[F, HikariTransactor[F]] = {
    for {
      ecFTP <- ExecutionContexts.fixedThreadPool(dbcfg.dbConfig.threadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[F](
        driverClassName = dbcfg.dbConfig.driver,
        url = dbcfg.dbConfig.url,
        user = dbcfg.dbConfig.user,
        pass = dbcfg.dbConfig.password,
        connectEC = ecFTP,
        logHandler = None
      )
    } yield xa
  }
}
