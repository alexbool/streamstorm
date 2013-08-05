package me.alexbool.streamstorm.instagram.client

import spray.http.HttpRequest
import spray.json._
import me.alexbool.streamstorm.instagram.{Coordinates, Location, Media}
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
    def read(json: JsValue) = json.asJsObject.getFields("data") match {
      case elems => elems.map(mediaReader.read _)
      case x @ _ => deserializationError(s"Expected array, but got something else: $x")
    }

    private val mediaReader = new RootJsonReader[Media] {
      def read(json: JsValue) = json.asJsObject.getFields("id", "tags", "location", "created_time") match {
        case JsString(id) :: JsArray(tags) :: JsObject(location) :: JsString(createdMillis) :: Nil =>
          Media(id, tags.map(_.toString()), Some(locationReader.read(JsObject(location))), new Instant(createdMillis))
        case JsString(id) :: JsArray(tags) :: JsNull :: JsString(createdMillis) :: Nil =>
          Media(id, tags.map(_.toString()), None, new Instant(createdMillis))
      }
    }

    private val locationReader = new RootJsonReader[Location] {
      def read(json: JsValue) = json.asJsObject.getFields("id", "name", "latitude", "longitude") match {
        case JsString(id) :: JsString(name) :: JsNumber(lat) :: JsNumber(long) :: Nil =>
          Location(id, name, Coordinates(BigDecimal(lat.toDouble), BigDecimal(long.toDouble)))
      }
    }
  }
}
