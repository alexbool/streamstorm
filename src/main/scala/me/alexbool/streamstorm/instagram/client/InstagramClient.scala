package me.alexbool.streamstorm.instagram.client

import akka.actor._
import akka.actor.Status.Failure
import akka.io.IO
import spray.can.Http
import spray.http. HttpResponse
import spray.json.JsonParser

class InstagramClient extends Actor with ActorLogging {
  private val httpTransport = IO(Http)(context.system)
  private val workerSequence = Iterator from 0

  def receive = {
    case m: Query[_] => {
      val sndr = context.sender
      context.actorOf(Props(new Worker(m, sndr)), s"worker-${workerSequence.next()}")
    }
  }

  private class Worker[R](query: Query[R], recipient: ActorRef) extends Actor with ActorLogging {
    override def preStart() {
      log.debug(s"Recipient: $recipient")
      httpTransport ! query.buildRequest
    }

    def receive = {
      case r: HttpResponse => handleResponse(r)
      case f: Failure      => handleFailure(f)
    }

    private[this] def handleResponse(r: HttpResponse) {
      try {
        log.debug(s"Handling response: $r")
        recipient ! query.responseParser.read(JsonParser(r.entity.asString))
      } catch {
        case e: Exception => {
          log.warning(s"Error during response parsing: $e")
          recipient ! Failure(e)
        }
      }
      context stop self
    }

    private[this] def handleFailure(f: Failure) {
      log.warning(s"Error during response processing: ${f.cause}")
      recipient ! f
      context stop self
    }
  }
}
