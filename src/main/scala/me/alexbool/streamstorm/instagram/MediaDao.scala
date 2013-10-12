package me.alexbool.streamstorm.instagram

import scala.concurrent.{Future, ExecutionContext}
import reactivemongo.bson._
import reactivemongo.api.collections.default.BSONCollection
import org.joda.time.Instant

class MediaDao(collection: BSONCollection)(implicit ec: ExecutionContext) {

  implicit private val mapper = new MediaMapper

  def insertIfNotExists(media: Media) = collection.insert(media)

  def findById(id: String): Future[Option[Media]] = {
    collection.find(BSONDocument("_id" -> id)).cursor[Media].headOption()
  }

  private class MediaMapper extends BSONDocumentReader[Media] with BSONDocumentWriter[Media] {
    def write(media: Media) = BSONDocument(
      "_id"         -> media.id,
      "tags"        -> media.tags,
      "images"      -> BSONDocument(
        "low"   -> writeImage(media.images.lowResolution),
        "std"   -> writeImage(media.images.standardResolution),
        "thumb" -> writeImage(media.images.thumbnail)
      ),
      "createdTime" -> BSONDateTime(media.createdTime.getMillis),
      "location"    -> media.location.map(loc => BSONArray(loc.longitude.toString(), loc.latitude.toString()))
    )

    private def writeImage(img: Image) =
      BSONDocument("width" -> img.width, "height" -> img.height, "url" -> img.url)

    def read(bson: BSONDocument): Media = {
      val imgsDoc = bson.getAs[BSONDocument]("images").get
      val images = Images(
        lowResolution = readImage(imgsDoc.getAs[BSONDocument]("low").get),
        standardResolution =  readImage(imgsDoc.getAs[BSONDocument]("std").get),
        thumbnail = readImage(imgsDoc.getAs[BSONDocument]("thumb").get)
      )
      Media(
        id          = bson.getAs[BSONString]("_id").get.value,
        tags        = bson.getAs[BSONArray]("tags").get.values.to[Seq].map(_.asInstanceOf[BSONString].value),
        location    = bson.getAs[BSONArray]("location").map(readLocation),
        images      = images,
        createdTime = new Instant(bson.getAs[BSONDateTime]("createdTime").get.value)
      )
    }

    private def readImage(doc: BSONDocument) = Image(
      width  = doc.getAs[BSONInteger]("width").get.value,
      height = doc.getAs[BSONInteger]("height").get.value,
      url    = doc.getAs[BSONString]("url").get.value
    )

    private def readLocation(array: BSONArray) = Location(
      longitude = BigDecimal(array.getAs[BSONString](0).get.value),
      latitude  = BigDecimal(array.getAs[BSONString](1).get.value)
    )
  }
}
