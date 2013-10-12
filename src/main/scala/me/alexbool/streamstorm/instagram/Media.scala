package me.alexbool.streamstorm.instagram

import org.joda.time.Instant

case class Media(id: String, tags: Seq[String], location: Option[Location], images: Images, createdTime: Instant)
case class Location(longitude: BigDecimal, latitude: BigDecimal)
case class Image(width: Int, height: Int, url: String)
case class Images(lowResolution: Image, standardResolution: Image, thumbnail: Image)
