package me.alexbool.streamstorm.instagram.client

import spray.http.HttpRequest
import spray.json._
import me.alexbool.streamstorm.instagram.Media
import me.alexbool.streamstorm.instagram.parsers.MediaResponseParser

sealed trait Query[R] {
  private[streamstorm] def buildRequest: HttpRequest
  private[streamstorm] def responseParser: RootJsonReader[R]
  def clientId: String
}

case class FindMediaByTag(tag: String, clientId: String) extends Query[Seq[Media]] {
  import spray.httpx.RequestBuilding._

  private[streamstorm] def buildRequest = Get(s"https://api.instagram.com/v1/tags/$tag/media/recent?client_id=$clientId")
  private[streamstorm] val responseParser = new MediaResponseParser
}
