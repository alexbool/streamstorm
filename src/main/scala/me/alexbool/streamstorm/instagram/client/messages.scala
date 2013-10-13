package me.alexbool.streamstorm.instagram.client

import spray.http.{HttpMethods, HttpMethod, HttpRequest}
import spray.httpx.RequestBuilding._
import spray.json._
import me.alexbool.streamstorm.instagram.Media

sealed trait Query[R] {
  private[streamstorm] def buildRequest(clientId: String): HttpRequest =
    new RequestBuilder(httpMethod).apply(buildUrl(clientId))

  private[streamstorm] def buildUrl(clientId: String): String
  private[streamstorm] def httpMethod: HttpMethod
  private[streamstorm] def responseParser: RootJsonReader[R]
}

sealed trait PageableQuery[R] extends Query[Seq[R]] {
  def limitResults: Int
  private[streamstorm] def buildRequestForNextPage(nextPageUrl: String): HttpRequest =
    new RequestBuilder(httpMethod).apply(nextPageUrl)
}

case class FindMediaByTag(tag: String, limitResults: Int = 16) extends PageableQuery[Media] {
  private[streamstorm] def buildUrl(clientId: String) =
    s"https://api.instagram.com/v1/tags/$tag/media/recent?client_id=$clientId"

  private[streamstorm] val responseParser = new MediaResponseParser
  private[streamstorm] val httpMethod = HttpMethods.GET
}
