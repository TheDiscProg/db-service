package dapex.dbservice.entities

import dapex.dbservice.entities.ServiceError.separator

sealed trait ServiceError extends Throwable with Product with Serializable {
  def message: String

  override def getMessage: String =
    s"${separator(super.getMessage)} $message".trim
}

object ServiceError {
  val separator: String => String = {
    case null => ""
    case s if s.trim.isEmpty => ""
    case s => s"$s ::"
  }

  case class DatabaseInsertionError(message: String) extends ServiceError
}
