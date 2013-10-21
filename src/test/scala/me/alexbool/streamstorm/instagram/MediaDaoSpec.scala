package me.alexbool.streamstorm.instagram

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}
import org.joda.time.Instant
import reactivemongo.api.MongoDriver
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Geo2DSpherical
import scala.concurrent.Await
import scala.concurrent.duration._

class MediaDaoSpec extends WordSpec with Matchers {
  val config = ConfigFactory.load("application.conf")
  val system = ActorSystem("MediaDaoSpec")
  implicit val ec = system.dispatcher
  val driver = new MongoDriver(system)
  val connection = driver.connection(Seq("localhost"))
  val db = connection("streamstorm")
  val collection = db("media")
  val dao = new MediaDao(collection)(system.dispatcher)

  Await.ready(
    db.indexesManager.onCollection("media").create(Index(key = Seq(("location", Geo2DSpherical)))),
    15.seconds
  )

  val media = Media(
    id          = "123456",
    tags        = Seq("rain"),
    location    = Some(Location(BigDecimal(90), BigDecimal(90))),
    images      = Images(
      lowResolution      = Image(360, 360, "http://google.com"),
      standardResolution = Image(640, 640, "http://google.com"),
      thumbnail          = Image(150, 150, "http://google.com")
    ),
    createdTime = new Instant
  )

  "MediaDao" should {
    "save Media instances" in {
      Await.result(dao.insertIfNotExists(media), 15.seconds) shouldEqual true
      Await.result(dao.insertIfNotExists(media), 15.seconds) shouldEqual false
      val found = Await.result(dao.findById(media.id), 15.seconds).get
      found shouldEqual media
    }
    "find by center point and since given time" in {
      val found = Await.result(dao.findByLocationSince(media.location.get, 100, media.createdTime), 15.seconds)
      found shouldEqual Seq(media)
    }
  }
}
