import sbt._

object Dependencies {

  lazy val all = Seq(
    "DAPEX" %% "dapex-messaging" % "0.1.6",
    "Shareprice" %% "shareprice-config" % "0.2.0",
    "org.typelevel" %% "cats-effect" % "3.4.8",
    "org.http4s" %% "http4s-dsl" % "0.23.18",
    "org.http4s" %% "http4s-ember-server" % "0.23.18",
    "org.http4s" %% "http4s-ember-client" % "0.23.18",
    "org.http4s" %% "http4s-circe" % "0.23.18",
    "io.circe" %% "circe-core" % "0.14.5",
    "io.circe" %% "circe-generic" % "0.14.5",
    "io.circe" %% "circe-parser" % "0.14.5",
    "io.circe" %% "circe-refined" % "0.14.5",
    "io.circe" %% "circe-generic-extras" % "0.14.3",
    "io.circe" %% "circe-config" % "0.10.0",
    "eu.timepit" %% "refined" % "0.10.2",
    "ch.qos.logback" % "logback-classic" % "1.4.11",
    "org.typelevel" %% "log4cats-core" % "2.6.0",
    "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC4" % Test,
    "org.tpolecat" %% "doobie-core" % "1.0.0-RC4",
    "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC4",
    "com.mysql" % "mysql-connector-j" % "8.1.0",
    "org.flywaydb" % "flyway-mysql" % "9.22.2",
    "io.scalaland" %% "chimney" % "0.7.5",
    "org.typelevel" %% "munit-cats-effect-2" % "1.0.7" % "test,it",
    "org.scalactic" %% "scalactic" % "3.2.15",
    "org.scalatest" %% "scalatest" % "3.2.15" % "test,it",
    "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % Test,
    "org.testcontainers" % "mysql" % "1.19.1" % IntegrationTest
  )
}
