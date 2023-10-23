package dapex.dbservice.fixture

import org.scalatest.concurrent._
import org.scalatest.time.{Millis, Seconds, Span}

trait DefaultFutureSetting extends ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(30, Seconds), interval = Span(100, Millis))

}
