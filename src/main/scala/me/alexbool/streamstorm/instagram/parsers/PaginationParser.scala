package me.alexbool.streamstorm.instagram.parsers

import spray.json.{JsString, JsValue, RootJsonReader}
import me.alexbool.streamstorm.instagram.Pagination

class PaginationParser extends RootJsonReader[Pagination] {
  def read(json: JsValue) = Pagination(json.asJsObject.fields("next_url").asInstanceOf[JsString].value)
}
