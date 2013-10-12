package me.alexbool.streamstorm.instagram.client

import org.scalatest.{Matchers, WordSpec}
import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._
import me.alexbool.streamstorm.instagram.Media
import language.postfixOps

class InstagramClientSpec extends WordSpec with Matchers {
  val actorSystem = ActorSystem("InstagramClientSpec")
  val client = actorSystem.actorOf(Props(new InstagramClient("ecf0f9cb0fea44bead53b8db458b447d")))
  implicit val timeout: Timeout = 15.seconds

  "Instagram API client" must {
    "execute /tags/$tag/media/recent queries" in {
      val result = Await.result(client ? FindMediaByTag("rain", 20), timeout.duration).asInstanceOf[Seq[Media]]
      result should have size 20
    }
  }
}
