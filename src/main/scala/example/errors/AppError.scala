package example.errors

sealed trait AppError extends Throwable

object AppError {
  case class InvalidBodyError(message: String) extends AppError
  
}
