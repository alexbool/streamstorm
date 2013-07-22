package me.alexbool.streamstorm.instagram.client

import akka.actor.{ActorLogging, Actor}
import akka.io.IO
import spray.can.Http

class InstagramClient extends Actor with ActorLogging {
  private val httpTransport = IO(Http)(context.system)

  def receive = {
    case FindMediaByTag(tag) =>
  }

  private class Worker[R](query: Query[R]) extends Actor with ActorLogging {
    def receive = ???
  }
}
