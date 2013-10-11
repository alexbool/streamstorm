package me.alexbool.streamstorm.instagram.parsers

import spray.json._
import me.alexbool.streamstorm.instagram.Location

class LocationSafeParser extends RootJsonReader[Option[Location]] {
  def read(json: JsValue) = json.asJsObject.getFields("latitude", "longitude") match {
    case Seq(JsNumber(lat), JsNumber(long)) => Some(Location(BigDecimal(lat.toDouble), BigDecimal(long.toDouble)))
    case Seq()                              => None
    case _ => deserializationError(s"Cannot parse location: ${json.compactPrint}")
  }
}
