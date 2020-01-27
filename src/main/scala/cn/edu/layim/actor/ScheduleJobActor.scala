package cn.edu.layim.actor

import akka.actor.{ Actor, ActorLogging }
import cn.edu.layim.actor.ActorMessage.OnlineUserMessage
import cn.edu.layim.websocket.WebSocketService

/**
 * 定时获取在线用户数
 *
 * @author 梦境迷离
 * @since 2020-01-27
 * @version v1.0
 */
class ScheduleJobActor extends Actor with ActorLogging {
  def receive = {
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