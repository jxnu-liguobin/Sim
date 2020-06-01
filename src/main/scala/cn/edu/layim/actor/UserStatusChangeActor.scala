package cn.edu.layim.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import cn.edu.layim.actor.ActorMessage.UserStatusChange
import cn.edu.layim.websocket.WebSocketService

/**
  * 用户在线状态切换&持久化
  *
 * 1.用户主动在线切换状态时
  * 2.用户连接创建和关闭时（可能频繁写库，状态数据不重要，可能没有这个必要，先注释掉，考虑同步定时？）
  * 3.用户状态检查时，直接操作Redis
  *
 * @author liguobin@growingio.com
  * @version 1.0,2020/6/1
  */
class UserStatusChangeActor extends Actor with ActorLogging {

  def receive = {
    case userStatusChange @ UserStatusChange(uId, typ) =>
      log.info(s"User status change => [$userStatusChange]")
      WebSocketService.changeOnline(uId, typ)
  }
}
