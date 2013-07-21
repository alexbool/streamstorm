package me.alexbool.streamstorm.instagram

import org.joda.time.Instant

case class Media(id: String, tags: List[String], location: Option[Location], createdTime: Instant)

case class Coordinates(longitude: BigDecimal, latitude: BigDecimal)
case class Location(id: String, name: String, coordinates: Coordinates)
