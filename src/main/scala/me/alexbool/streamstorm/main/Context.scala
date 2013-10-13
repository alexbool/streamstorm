package me.alexbool.streamstorm.main

import akka.actor.{PoisonPill, Props, ActorSystem}
import com.typesafe.config.{Config, ConfigFactory}
import reactivemongo.api.MongoDriver
import me.alexbool.streamstorm.instagram.MediaDao
import me.alexbool.streamstorm.instagram.client.InstagramClient
import scala.collection.JavaConversions._
import java.io.Closeable

class Context extends Closeable {
  val config: Config = ConfigFactory.load("application.conf")
  val system = ActorSystem("streamstorm")
  implicit val executionCOntext = system.dispatcher

  val driver = new MongoDriver(system)
  val connection = driver.connection(config.getStringList("mongo.hosts").toList)
  val db = connection("streamstorm")
  val collection = db("media")
  val mediaDao = new MediaDao(collection)

  val instagramClient = system.actorOf(Props(new InstagramClient("ecf0f9cb0fea44bead53b8db458b447d")))

  def close() {
    instagramClient ! PoisonPill
    system.shutdown()
    driver.close()
  }
}
