package me.alexbool.streamstorm.instagram.parsers

import spray.json._
import org.joda.time.Instant
import me.alexbool.streamstorm.instagram.Media

class MediaParser extends RootJsonReader[Media] {
  def read(json: JsValue) = json.asJsObject.getFields("id", "tags", "location", "created_time") match {
    case Seq(JsString(id), JsArray(tags), JsObject(location), JsString(createdMillis)) =>
      Media(id, tags.map(_.asInstanceOf[JsString].value), Some((new LocationParser).read(JsObject(location))), new Instant(createdMillis.toLong))
    case Seq(JsString(id), JsArray(tags), JsNull, JsString(createdMillis)) =>
      Media(id, tags.map(_.asInstanceOf[JsString].value), None, new Instant(createdMillis.toLong))
    case _ => deserializationError("Cannot deserialize media")
  }
}
