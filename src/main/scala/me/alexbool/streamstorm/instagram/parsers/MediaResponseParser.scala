package me.alexbool.streamstorm.instagram.parsers

import spray.json._
import me.alexbool.streamstorm.instagram.Media

class MediaResponseParser extends RootJsonReader[Seq[Media]] {
  def read(json: JsValue) = json.asJsObject
          .fields("data").asInstanceOf[JsArray]
          .elements
          .map((new MediaParser).read _)
}
