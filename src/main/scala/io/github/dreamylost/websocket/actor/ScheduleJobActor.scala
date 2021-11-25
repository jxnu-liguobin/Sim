package io.github.dreamylost.websocket.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import io.github.dreamylost.websocket.WebSocketService
import io.github.dreamylost.websocket.actor.ActorMessage.OnlineUserMessage

/** 定时获取在线用户数
  *
  * @author 梦境迷离
  * @since 2020-01-27
  * @version v1.0
  */
class ScheduleJobActor extends Actor with ActorLogging {
  def receive: Receive = {
    case OnlineUserMessage => {
      val onlineTotal = WebSocketService.getConnections
      //使用websocket展现到页面？
      log.info(s"Online user total => [total User = $onlineTotal]")
    }
  }

  override def unhandled(message: Any): Unit = {
    log.warning(s"No Mapping Message => [msg = $message]!")
  }

}
