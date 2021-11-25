package io.github.dreamylost.websocket.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import io.github.dreamylost.websocket.Protocols._
import io.github.dreamylost.websocket.WebSocketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/** 用户在线状态切换&持久化
  *
  * 1.用户主动在线切换状态时
  * 2.用户连接创建和关闭时（可能频繁写库，状态数据不重要，可能没有这个必要，先注释掉，考虑同步定时？）
  * 3.用户状态检查时，直接操作Redis
  *
  * @author 梦境迷离
  * @version 1.0,2020/6/1
  */
@Component("userStatusChangeActor")
@Scope("prototype")
class UserStatusChangeActor extends Actor with ActorLogging {

  @Autowired
  private var wsService: WebSocketService = _

  def receive: Receive = { case userStatusChange @ UserStatusChange(uId, typ) =>
    log.info(s"User status change => [$userStatusChange]")
    wsService.changeOnline(uId, typ)
  }
}
