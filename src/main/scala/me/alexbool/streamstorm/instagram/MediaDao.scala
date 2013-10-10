package me.alexbool.streamstorm.instagram

import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.ExecutionContext
import reactivemongo.bson.{BSONArray, BSONDateTime, BSONDocument}

class MediaDao(collection: BSONCollection)(implicit ec: ExecutionContext) {

  def save(media: Media) {
    val doc = BSONDocument(
      "_id"         -> media.id,
      "tags"        -> media.tags,
      "createdTime" -> BSONDateTime(media.createdTime.getMillis),
      "location"    -> media.location.map(loc => BSONArray(loc.longitude.toString(), loc.latitude.toString()))
    )
    collection.save(doc)
  }
}
