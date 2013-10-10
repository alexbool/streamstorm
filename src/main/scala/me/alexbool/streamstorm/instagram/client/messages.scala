package me.alexbool.streamstorm.instagram.client

import spray.http.HttpRequest
import spray.json._
import me.alexbool.streamstorm.instagram.{Location, Media}
import org.joda.time.Instant

sealed trait Query[R] {
  private[streamstorm] def buildRequest: HttpRequest
  private[streamstorm] def responseParser: RootJsonReader[R]
  def clientId: String
}

case class FindMediaByTag(tag: String, clientId: String) extends Query[Seq[Media]] {
  import spray.httpx.RequestBuilding._

  private[streamstorm] def buildRequest = Get(s"https://api.instagram.com/v1/tags/$tag/media/recent?client_id=$clientId")

  private[streamstorm] val responseParser = new RootJsonReader[Seq[Media]] {
    def read(json: JsValue) = json.asJsObject
      .fields("data").asInstanceOf[JsArray]
      .elements
      .map(mediaReader.read _)

    private val mediaReader = new RootJsonReader[Media] {
      def read(json: JsValue) = json.asJsObject.getFields("id", "tags", "location", "created_time") match {
        case Seq(JsString(id), JsArray(tags), JsObject(location), JsString(createdMillis)) =>
          Media(id, tags.map(_.toString()), Some(locationReader.read(JsObject(location))), new Instant(createdMillis.toLong))
        case Seq(JsString(id), JsArray(tags), JsNull, JsString(createdMillis)) =>
          Media(id, tags.map(_.toString()), None, new Instant(createdMillis.toLong))
        case _ => deserializationError("Cannot deserialize media")
      }
    }

    private val locationReader = new RootJsonReader[Location] {
      def read(json: JsValue) = json.asJsObject.getFields("latitude", "longitude") match {
        case Seq(JsNumber(lat), JsNumber(long)) =>
          Location(BigDecimal(lat.toDouble), BigDecimal(long.toDouble))
      }
    }
  }
}
