package exceptions

case class LoginException() extends Exception("Unable to parse input, expected (login, password)")
