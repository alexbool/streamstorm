package me.alexbool.streamstorm.instagram

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.scalatest.{Matchers, WordSpec}
import org.joda.time.Instant
import reactivemongo.api.MongoDriver
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

  "MediaDao" should {
    "save Media instances" in {
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
      Await.result(dao.save(media), 15.seconds).ok should be (true)

      val found = Await.result(dao.findById(media.id), 15.seconds).get
      found shouldEqual media
    }
  }
}
