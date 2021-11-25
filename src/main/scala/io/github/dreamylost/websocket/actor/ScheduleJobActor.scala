package io.github.dreamylost.websocket.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import io.github.dreamylost.websocket.Protocols._
import io.github.dreamylost.websocket.WebSocketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/** 定时获取在线用户数
  *
  * @author 梦境迷离
  * @since 2020-01-27
  * @version v1.0
  */
@Component("scheduleJobActor")
@Scope("prototype")
class ScheduleJobActor extends Actor with ActorLogging {

  @Autowired
  private var wsService: WebSocketService = _

  def receive: Receive = { case OnlineUserMessage =>
    val onlineTotal = wsService.getConnections
    //使用websocket展现到页面？
    log.info(s"Online user total => [total User = $onlineTotal]")
  }

  override def unhandled(message: Any): Unit = {
    log.warning(s"No Mapping Message => [msg = $message]!")
  }

}
