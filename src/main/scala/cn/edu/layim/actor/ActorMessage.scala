package cn.edu.layim.actor

import akka.actor.ActorRef
import cn.edu.layim.entity.Message
import com.google.gson.Gson

object ActorMessage {

  final lazy val gson: Gson = new Gson

  case class TransmitMessage(uId: Int, msg: String, originActorRef: ActorRef) {
    def getMessage = {
      val message: Message = gson.fromJson(msg.replaceAll("type", "Type"), classOf[Message])
      message
    }
  }

  case object OnlineUserMessage

}