package dapex.dbservice.domain.db.connector

import cats.Monad
import doobie.util.log
import doobie.util.log.LogHandler
import org.typelevel.log4cats.Logger
import cats.implicits._

class DatabaseLogger[F[_]: Monad: Logger] extends LogHandler[F] {

  override def run(logEvent: log.LogEvent): F[Unit] =
    logEvent match {
      case log.Success(sql, args, label, exec, processing) =>
        for {
          _ <- Logger[F].info(
            s"""Succesful Statement Execution
               | ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
               |
               | arguments = [${args.mkString(", ")}]
               | label     = $label
               | elapsed = ${exec.toMillis.toString} ms exec + ${processing.toMillis.toString} ms processing (failed) (${(exec + processing).toMillis.toString} ms total)
               |
               |""".stripMargin
          )
        } yield ()
      case log.ProcessingFailure(sql, args, label, exec, processing, failure) =>
        Logger[F].warn(
          s"""Failed ResultSet Processing:
             |
             | ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
             |
             | arguments = [${args.mkString(", ")}]
             | label     = $label
             | elapsed = ${exec.toMillis.toString} ms exec + ${processing.toMillis.toString} ms processing (failed) (${(exec + processing).toMillis.toString} ms total)
             | failure = ${failure.getMessage}
                  """.stripMargin
        )
      case log.ExecFailure(sql, args, label, exec, failure) =>
        Logger[F].error(
          s"""Failed Statement Execution:
             |
             | ${sql.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
             |
             | arguments = [${args.mkString(", ")}]
             | label     = $label
             | elapsed = ${exec.toMillis.toString} ms exec (failed)
             | failure = ${failure.getMessage}
                  """.stripMargin
        )
    }

}
