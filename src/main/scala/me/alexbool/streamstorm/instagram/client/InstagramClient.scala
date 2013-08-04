package me.alexbool.streamstorm.instagram.client

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import akka.io.IO
import spray.can.Http
import spray.http.{HttpRequest, HttpResponse}
import akka.actor.Status.Failure
import spray.json.JsonParser

class InstagramClient extends Actor with ActorLogging {
  private val httpTransport = IO(Http)(context.system)
  private val workerSequence = Iterator from 0

  def receive = {
    case m: FindMediaByTag => context.actorOf(Props(new Worker(m, sender)), s"worker-${workerSequence.next()}")
  }

  private class Worker[R](query: Query[R], recipient: ActorRef) extends Actor with ActorLogging {
    override def preStart() {
      httpTransport ! HttpRequest()
    }

    def receive = {
      case r: HttpResponse => handleResponse(r)
      case f: Failure      => handleFailure(f)
    }

    private[this] def handleResponse(r: HttpResponse) {
      recipient ! query.responseParser.read(JsonParser(r.entity.asString))
      context stop self
    }

    private[this] def handleFailure(f: Failure) {
      recipient ! f
      context stop self
    }
  }
}
