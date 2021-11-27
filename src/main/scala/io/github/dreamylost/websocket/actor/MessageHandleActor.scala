package io.github.dreamylost.websocket.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import io.github.dreamylost.util.Jackson
import io.github.dreamylost.websocket.Protocols._
import io.github.dreamylost.websocket.Protocols
import io.github.dreamylost.websocket.WebSocketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

import java.util

/** 处理websocket的消息分发
  *
  * @author 梦境迷离
  * @since 2020-01-24
  * @version v1.2
  */
@Component("messageHandleActor")
@Scope("prototype")
class MessageHandleActor extends Actor with ActorLogging {

  @Autowired
  private var wsService: WebSocketService = _

  override def receive: Receive = { case tm: TransmitMessage =>
    log.info(s"来自客户端的消 => [msg = $tm]")
    val protocol = Protocols.ImProtocol.unStringify(tm.getMessage.`type`)
    protocol match {
      case ImProtocol.readOfflineMessage =>
        wsService.readOfflineMessage(tm.getMessage)
      case ImProtocol.message =>
        wsService.sendMessage(tm.getMessage)
      case ImProtocol.checkOnline =>
        val result: util.HashMap[String, String] = wsService.checkOnline(tm.getMessage)
        wsService.sendMessage(Jackson.mapper.writeValueAsString(result), tm.originActorRef)
      case ImProtocol.addGroup =>
        wsService.addGroup(tm.uId, tm.getMessage)
      case ImProtocol.changOnline =>
        wsService.changeOnline(tm.uId, tm.getMessage.msg)
      case ImProtocol.addFriend =>
        wsService.addFriend(tm.uId, tm.getMessage)
      case ImProtocol.agreeAddFriend =>
        val actor = wsService.actorRefSessions.get(tm.getMessage.to.id)
        if (actor != null) {
          wsService.sendMessage(tm.msg, actor)
        }
      case ImProtocol.agreeAddGroup =>
        wsService.agreeAddGroup(tm.getMessage)
      case ImProtocol.refuseAddGroup =>
        wsService.refuseAddGroup(tm.getMessage)
      case ImProtocol.unHandMessage =>
        val result = wsService.countUnHandMessage(tm.uId)
        wsService.sendMessage(Jackson.mapper.writeValueAsString(result), tm.originActorRef)
      case ImProtocol.delFriend =>
        wsService.removeFriend(tm.uId, tm.getMessage.to.id)
      case _ =>
        log.warning("No Mapping Message!")
    }
  }

  override def unhandled(message: Any): Unit = {
    log.warning(s"No Mapping Message => [msg = $message]!")
  }
}
