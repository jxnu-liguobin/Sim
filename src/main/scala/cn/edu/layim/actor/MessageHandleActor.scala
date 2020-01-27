package cn.edu.layim.actor

import java.util

import akka.actor.{ Actor, ActorLogging }
import cn.edu.layim.actor.ActorMessage._
import cn.edu.layim.websocket.WebSocketService

/**
 * 处理websocket的消息分发
 *
 * @author 梦境迷离
 * @since 2020-01-24
 * @version v1.0
 */
class MessageHandleActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case tm: TransmitMessage =>
      log.info(s"来自客户端的消 => [msg = $tm]")
      tm.getMessage.getType match {
        case "message" => {
          WebSocketService.sendMessage(tm.getMessage)
        }
        case "checkOnline" => {
          val result: util.HashMap[String, String] = WebSocketService.checkOnline(tm.getMessage, tm.originActorRef)
          WebSocketService.sendMessage(gson.toJson(result), tm.originActorRef)
        }
        case "addGroup" => {
          WebSocketService.addGroup(tm.uId, tm.getMessage)
        }
        case "changOnline" => {
          WebSocketService.changeOnline(tm.uId, tm.getMessage.getMsg)
        }
        case "addFriend" => {
          WebSocketService.addFriend(tm.uId, tm.getMessage)
        }
        case "agreeAddFriend" => {
          if (WebSocketService.actorRefSessions.get(tm.getMessage.getTo.getId) != null) {
            WebSocketService.sendMessage(tm.msg, WebSocketService.actorRefSessions.get(tm.getMessage.getTo.getId))
          }
        }
        case "agreeAddGroup" => {
          WebSocketService.agreeAddGroup(tm.getMessage)
        }
        case "refuseAddGroup" => {
          WebSocketService.refuseAddGroup(tm.getMessage);
        }
        case "unHandMessage" => {
          val result = WebSocketService.countUnHandMessage(tm.uId)
          WebSocketService.sendMessage(gson.toJson(result), tm.originActorRef)
        }
        case "delFriend" => {
          WebSocketService.removeFriend(tm.uId, tm.getMessage.getTo.getId)
        }
        case _ => {
          log.warning("No Mapping Message!")
        }
      }
  }

  override def unhandled(message: Any): Unit = {
    log.warning(s"No Mapping Message => [msg = $message]!")
  }
}