package me.alexbool.streamstorm.instagram.parsers

import spray.json._
import org.joda.time.Instant
import me.alexbool.streamstorm.instagram.{Media, Location, Pagination, Image, Images}

class MediaParser extends RootJsonReader[Media] {
  def read(json: JsValue) = json.asJsObject.getFields("id", "tags", "location", "images", "created_time") match {
    case Seq(JsString(id), JsArray(tags), location: JsObject, images: JsObject, JsString(createdMillis)) =>
      Media(id,
        tags.map(_.asInstanceOf[JsString].value),
        (new LocationSafeParser).read(location),
        (new ImagesParser).read(images),
        new Instant(createdMillis.toLong))
    case Seq(JsString(id), JsArray(tags), JsNull, images: JsObject, JsString(createdMillis)) =>
      Media(id,
        tags.map(_.asInstanceOf[JsString].value),
        None,
        (new ImagesParser).read(images),
        new Instant(createdMillis.toLong))
    case _ => deserializationError("Cannot deserialize media")
  }
}

class LocationSafeParser extends RootJsonReader[Option[Location]] {
  def read(json: JsValue) = json.asJsObject.getFields("latitude", "longitude") match {
    case Seq(JsNumber(lat), JsNumber(long)) => Some(Location(BigDecimal(lat.toDouble), BigDecimal(long.toDouble)))
    case Seq()                              => None
    case _ => deserializationError(s"Cannot parse location: ${json.compactPrint}")
  }
}

class ImageParser extends RootJsonReader[Image] {
  def read(json: JsValue) = json.asJsObject.getFields("width", "height", "url") match {
    case Seq(JsNumber(width), JsNumber(height), JsString(url)) => Image(width.toInt, height.toInt, url)
    case _ => deserializationError(s"Cannot parse image: ${json.compactPrint}")
  }
}

class ImagesParser extends RootJsonReader[Images] {
  private val imageParser = new ImageParser
  def read(json: JsValue) = json.asJsObject.getFields("low_resolution", "standard_resolution", "thumbnail") match {
    case Seq(low: JsObject, std: JsObject, thumb: JsObject) =>
      Images(imageParser.read(low), imageParser.read(std), imageParser.read(thumb))
    case _ => deserializationError(s"Cannot parse images: ${json.compactPrint}")
  }
}

class MediaResponseParser extends RootJsonReader[Seq[Media]] {
  def read(json: JsValue) = json.asJsObject
          .fields("data").asInstanceOf[JsArray]
          .elements
          .map((new MediaParser).read _)
}

class PaginationParser extends RootJsonReader[Pagination] {
  def read(json: JsValue) = Pagination(json.asJsObject.fields("next_url").asInstanceOf[JsString].value)
}
