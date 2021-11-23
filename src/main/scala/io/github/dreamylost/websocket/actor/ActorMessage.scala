package io.github.dreamylost.websocket.actor

import akka.actor.ActorRef
import com.google.gson.Gson
import io.github.dreamylost.model.entity.Message

/**
  *
 * @author 梦境迷离
  * @since 2020-01-27
  * @version v1.0
  */
object ActorMessage {

  final lazy val gson: Gson = new Gson

  case class TransmitMessage(uId: Int, msg: String, originActorRef: ActorRef) {
    def getMessage = {
      val message: Message = gson.fromJson(msg, classOf[Message])
      message
    }
  }

  case object OnlineUserMessage

  case class UserStatusChange(uId: Int, typ: String)

}
