package me.alexbool.streamstorm.instagram.client

import akka.actor.Status.Failure
import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.io.IO
import spray.http.HttpResponse
import spray.json.JsonParser
import spray.can.Http
import me.alexbool.streamstorm.instagram.parsers.PaginationParser

private [streamstorm] sealed abstract class WorkerBase[R](query: Query[R],
                                                          clientId: String,
                                                          recipient: ActorRef,
                                                          httpTransport: ActorRef)
  extends Actor with ActorLogging {

  override def preStart() {
    log.debug(s"Recipient: $recipient")
    httpTransport ! query.buildRequest(clientId)
  }

  def receive = {
    case r: HttpResponse => handleResponse(r)
    case f: Failure      => handleFailure(f)
  }

  protected def handleResponse(r: HttpResponse) {
    try {
      log.debug(s"Handling response: $r")
      doWithResponse(r)
    } catch {
      case e: Exception => {
        log.warning(s"Error during response parsing: $e")
        recipient ! Failure(e)
        context stop self
      }
    }
  }

  protected def doWithResponse(r: HttpResponse)

  protected def handleFailure(f: Failure) {
    log.warning(s"Error during response processing: ${f.cause}")
    recipient ! f
    context stop self
  }
}

private[streamstorm] class Worker[R](query: Query[R], clientId: String, recipient: ActorRef, httpTransport: ActorRef)
        extends WorkerBase[R](query, clientId, recipient, httpTransport) {

  protected def doWithResponse(r: HttpResponse) {
    recipient ! query.responseParser.read(JsonParser(r.entity.asString))
    context stop self
  }
}

private[streamstorm] class PageableWorker[R](query: PageableQuery[R],
                                             clientId: String,
                                             recipient: ActorRef,
                                             httpTransport: ActorRef)
  extends WorkerBase[Seq[R]](query, clientId, recipient, httpTransport) {

  private[this] val pageCounter = Iterator from 1
  private[this] val pages = collection.mutable.Buffer[Seq[R]]()

  override def preStart() {
    log.debug(s"Downloading page ${pageCounter.next()}")
    super.preStart()
  }

  protected def doWithResponse(r: HttpResponse) {
    val json = JsonParser(r.entity.asString)
    val arrivedPage = query.responseParser.read(json)
    pages += arrivedPage
    val currentItemCount = pages.map(_.size).sum
    if (currentItemCount < query.limitResults) {
      val pagination = (new PaginationParser).read(json.asJsObject.fields("pagination"))
      log.debug(s"Downloading page ${pageCounter.next()}")
      httpTransport ! query.buildRequestForNextPage(pagination.nextUrl)
    } else {
      log.debug(s"All requested content downloaded in ${pageCounter.next() - 1} pages")
      recipient ! pages.flatten.take(query.limitResults)
      context stop self
    }
  }
}
