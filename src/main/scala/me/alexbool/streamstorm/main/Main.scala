package me.alexbool.streamstorm.main

object Main extends App {
  var context: Context = null // XXX Greater control on init process
  try {
    context = new Context
  } finally {
    context.close()
  }
}
