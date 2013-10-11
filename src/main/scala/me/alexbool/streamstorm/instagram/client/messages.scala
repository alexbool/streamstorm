package me.alexbool.streamstorm.instagram.client

import spray.http.HttpRequest
import spray.httpx.RequestBuilding._
import spray.json._
import me.alexbool.streamstorm.instagram.Media
import me.alexbool.streamstorm.instagram.parsers.MediaResponseParser

sealed trait Query[R] {
  private[streamstorm] def buildRequest(clientId: String): HttpRequest
  private[streamstorm] def responseParser: RootJsonReader[R]
}

case class FindMediaByTag(tag: String) extends Query[Seq[Media]] {
  private[streamstorm] def buildRequest(clientId: String) =
    Get(s"https://api.instagram.com/v1/tags/$tag/media/recent?client_id=$clientId")

  private[streamstorm] val responseParser = new MediaResponseParser
}
