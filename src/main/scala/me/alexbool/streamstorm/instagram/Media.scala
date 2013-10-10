package me.alexbool.streamstorm.instagram

import org.joda.time.Instant

case class Media(id: String, tags: Seq[String], location: Option[Location], createdTime: Instant)

case class Location(longitude: BigDecimal, latitude: BigDecimal)
