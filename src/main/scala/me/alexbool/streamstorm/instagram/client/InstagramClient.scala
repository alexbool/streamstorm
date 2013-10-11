package me.alexbool.streamstorm.instagram.client

import akka.actor._
import akka.actor.Status.Failure
import akka.io.IO
import spray.can.Http
import spray.http. HttpResponse
import spray.json.JsonParser

class InstagramClient(clientId: String) extends Actor with ActorLogging {
  private val workerSequence = Iterator from 0

  def receive = {
    case m: Query[_] => {
      val sndr = context.sender
      context.actorOf(Props(new Worker(m, clientId, sndr)), s"worker-${workerSequence.next()}")
    }
  }
}
