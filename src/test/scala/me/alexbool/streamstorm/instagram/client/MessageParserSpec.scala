package me.alexbool.streamstorm.instagram.client

import org.scalatest.{Matchers, WordSpec}
import spray.json.JsonParser

class MessageParserSpec extends WordSpec with Matchers {
  "Instagram parsers" must {
    "parse 'find media by tag' response" in {
      val json = io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("tags-endpoint-response.json")).mkString
      val media = (new MediaResponseParser).read(JsonParser(json))
      media should have size 16

      val firstMedia = media.head
      firstMedia.id should be ("564299882475041654_7268595")
      firstMedia.createdTime.getMillis should be (1381489809)
      firstMedia.tags should be (Seq("fun", "smile", "mygirl", "egg", "rain"))
      firstMedia.location should be (None)
    }
  }
}
