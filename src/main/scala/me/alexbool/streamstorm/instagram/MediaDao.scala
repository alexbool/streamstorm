package me.alexbool.streamstorm.instagram

import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.ExecutionContext
import reactivemongo.bson.{BSONArray, BSONDateTime, BSONDocument}

class MediaDao(collection: BSONCollection)(implicit ec: ExecutionContext) {

  def save(media: Media) {
    val doc = BSONDocument(
      "_id"         -> media.id,
      "tags"        -> media.tags,
      "images"      -> BSONDocument(
        "low"   -> mapImage(media.images.lowResolution),
        "std"   -> mapImage(media.images.standardResolution),
        "thumb" -> mapImage(media.images.thumbnail)
      ),
      "createdTime" -> BSONDateTime(media.createdTime.getMillis),
      "location"    -> media.location.map(loc => BSONArray(loc.longitude.toString(), loc.latitude.toString()))
    )
    collection.save(doc)
  }

  private def mapImage(img: Image) =
    BSONDocument("width" -> img.width, "height" -> img.height, "url" -> img.url)
}
