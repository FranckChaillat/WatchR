package dataaccess

trait ApiRepository {
  def baseUri: String
  def httpConnector: HttpConnector
}
