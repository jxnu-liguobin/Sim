package io.github.dreamylost.config

import io.github.dreamylost.websocket.WebSocketServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  * @author 梦境迷离
  * @version 1.0,2021/11/29
  */
@Component
class ApplicationStartup @Autowired()(webSocketServer: WebSocketServer)
  extends ApplicationListener[ApplicationReadyEvent] {
  
  override def onApplicationEvent(event: ApplicationReadyEvent): Unit = {
    Future.apply(webSocketServer.startUp())
  }
  
}
