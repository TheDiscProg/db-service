package dapex.dbservice.entities

import com.zaxxer.hikari.HikariConfig
import dapex.config.DatabaseConfig
import io.scalaland.chimney.Transformer

case class MysqlConfig(
    dbConfig: DatabaseConfig
)

object MysqlConfig {

  def fromOption(option: Option[DatabaseConfig]): MysqlConfig =
    option match {
      case Some(cfg) => MysqlConfig(cfg)
      case _ => throw new RuntimeException(s"Database Configuration missing")
    }

  implicit val transformIntoHikariConfig =
    new Transformer[MysqlConfig, HikariConfig] {
      override def transform(src: MysqlConfig): HikariConfig = {
        val config = new HikariConfig()
        config.setJdbcUrl(src.dbConfig.url)
        config.setUsername(src.dbConfig.user)
        config.setPassword(src.dbConfig.password)
        config.setDriverClassName(src.dbConfig.driver)
        config.setConnectionTimeout(3000L)
        config.setMaximumPoolSize(src.dbConfig.connectionPoolSize)
        config
      }
    }
}
