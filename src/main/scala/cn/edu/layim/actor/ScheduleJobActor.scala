package cn.edu.layim.actor

import akka.actor.{ Actor, ActorLogging }
import cn.edu.layim.actor.ActorMessage.OnlineUserMessage
import cn.edu.layim.websocket.WebSocketService

class ScheduleJobActor extends Actor with ActorLogging {
  def receive = {
    case OnlineUserMessage => {
      val onlineTotal = WebSocketService.getConnections
      log.info(s"Online user total => [total User = $onlineTotal]")
    }
  }

  override def unhandled(message: Any): Unit = {
    log.warning(s"No Mapping Message => [msg = $message]!")
  }

}