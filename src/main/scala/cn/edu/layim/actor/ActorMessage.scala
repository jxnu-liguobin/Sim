package cn.edu.layim.actor

import akka.actor.ActorRef
import cn.edu.layim.entity.Message
import com.google.gson.Gson

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
      val message: Message = gson.fromJson(msg.replaceAll("type", "Type"), classOf[Message])
      message
    }
  }

  case object OnlineUserMessage

}
