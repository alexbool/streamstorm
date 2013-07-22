package me.alexbool.streamstorm.instagram.client

import spray.http.Uri
import spray.json.JsObject
import me.alexbool.streamstorm.instagram.Media

sealed trait Query[R] {
  private[streamstorm] def toUri: Uri
  private[streamstorm] def responseParser: JsObject => R
}
case class FindMediaByTag(tag: String) extends Query[Seq[Media]] {
  def toUri = ???
  def responseParser = ???
}
