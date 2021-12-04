package io.github.dreamylost.websocket

import akka.actor.ActorRef
import io.github.dreamylost.model.entities.Message
import io.github.dreamylost.util.Jackson
import io.github.dreamylost.model.Mine

/** @author 梦境迷离
  * @version 1.0,2021/11/25
  */
object Protocols {

  sealed trait ImProtocol {
    self =>
    @inline final def stringify: String = self match {
      case ImProtocol.readOfflineMessage => "readOfflineMessage"
      case ImProtocol.message => "message"
      case ImProtocol.checkOnline => "checkOnline"
      case ImProtocol.addGroup => "addGroup"
      case ImProtocol.changOnline => "changOnline"
      case ImProtocol.addFriend => "addFriend"
      case ImProtocol.agreeAddFriend => "agreeAddFriend"
      case ImProtocol.agreeAddGroup => "agreeAddGroup"
      case ImProtocol.refuseAddGroup => "refuseAddGroup"
      case ImProtocol.unHandMessage => "unHandMessage"
      case ImProtocol.delFriend => "delFriend"
    }
  }

  object ImProtocol {

    @inline final def unStringify(`type`: String): ImProtocol = {
      val mapping = Map(
        ImProtocol.readOfflineMessage.stringify -> readOfflineMessage,
        ImProtocol.message.stringify -> message,
        ImProtocol.checkOnline.stringify -> checkOnline,
        ImProtocol.addGroup.stringify -> addGroup,
        ImProtocol.changOnline.stringify -> changOnline,
        ImProtocol.addFriend.stringify -> addFriend,
        ImProtocol.agreeAddFriend.stringify -> agreeAddFriend,
        ImProtocol.agreeAddGroup.stringify -> agreeAddGroup,
        ImProtocol.refuseAddGroup.stringify -> refuseAddGroup,
        ImProtocol.unHandMessage.stringify -> unHandMessage,
        ImProtocol.delFriend.stringify -> delFriend
      )

      mapping(`type`)

    }

    case object readOfflineMessage extends ImProtocol

    case object message extends ImProtocol

    case object checkOnline extends ImProtocol

    case object addGroup extends ImProtocol

    case object changOnline extends ImProtocol

    case object addFriend extends ImProtocol

    case object agreeAddFriend extends ImProtocol

    case object agreeAddGroup extends ImProtocol

    case object refuseAddGroup extends ImProtocol

    case object unHandMessage extends ImProtocol

    case object delFriend extends ImProtocol
  }

  /** 添加群信息
    */
  case class Group(groupId: Int, remark: String)

  /** 同意添加群
    */
  case class AddRefuseMessage(toUid: Int, groupId: Int, messageBoxId: Int, mine: Mine)

  case class TransmitMessage(uId: Int, msg: String, originActorRef: ActorRef) {
    def getMessage: Message = Jackson.mapper.readValue[Message](msg)
  }

  case object OnlineUserMessage

  case class UserStatusChange(uId: Int, typ: String)

}
