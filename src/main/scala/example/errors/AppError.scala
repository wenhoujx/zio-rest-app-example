package example.errors

import example.models.*

sealed trait AppError extends Throwable

object AppError {
  case class InvalidBodyError(message: String) extends AppError
  case class IllegalMarriageError(message: String) extends AppError
  val CannotSelfMarryError = IllegalMarriageError(
    "call me old fashioned, but one can't marry themselves."
  )
  case class MissingUserError(id: UserId) extends AppError:
    override def getMessage(): String = s"missing user ${id.id}"
  case class AlreadyMarriedError(id: UserId, marriedTo: UserId)
      extends AppError:
    override def getMessage(): String =
      s"already married (${id.id}, ${marriedTo.id})"
  case class NotMarriedError(idA: UserId, idB: UserId) extends AppError:
    override def getMessage(): String =
      s"can't divorce unmarried couple: (${idA.id}, ${idB.id})"

}
