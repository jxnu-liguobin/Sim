package io.github.dreamylost.websocket

import akka.actor.ActorRef
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.ScalaObjectMapper
import io.github.dreamylost.constant.SystemConstant
import org.bitlap.tools.log
import org.bitlap.tools.logs.LogType
import io.github.dreamylost.model.domains.Add
import io.github.dreamylost.model.domains.Receive
import io.github.dreamylost.model.entities.AddMessage
import io.github.dreamylost.model.entities.Message
import io.github.dreamylost.model.entities.User
import io.github.dreamylost.service.UserService
import io.github.dreamylost.util.DateUtil
import io.github.dreamylost.util.Jackson
import io.github.dreamylost.websocket.Protocols.ImProtocol
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util
import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

/** WebSocket
  *
  * 锁对象的使用有待优化
  *
  * @since 2020年01月23日
  * @author 梦境迷离
  * @version 1.2
  */
@Service
@log(logType = LogType.Slf4j)
class WebSocketService @Autowired() (
    redisService: RedisService,
    objectMapper: ObjectMapper with ScalaObjectMapper
) {
  @Autowired
  private var userService: UserService = _

  final lazy val actorRefSessions: ConcurrentHashMap[Integer, ActorRef] =
    new ConcurrentHashMap[Integer, ActorRef]

  /** 发送消息
    *
    * @param message
    */
  def sendMessage(message: Message): Unit =
    message.synchronized {
      log.debug(s"好友消息或群消息 => [msg = $message]")
      //封装返回消息格式
      val gid = message.to.id
      val receive = getReceive(message)
      //聊天类型，可能来自朋友或群组
      if (SystemConstant.FRIEND_TYPE == message.to.`type`) {
        val us = userService.findUserById(gid)
        if (us == null) return
        val msg = if (actorRefSessions.containsKey(gid)) {
          val actorRef = actorRefSessions.get(gid)
          val tmpReceiveArchive = receive.copy(status = 1)
          sendMessage(receiveStr(tmpReceiveArchive), actorRef)
          tmpReceiveArchive
        } else receive
        userService.saveMessage(msg)
      } else {
        buildGroupMessage(message, receive, gid)
      }
    }

  private def receiveStr(r: Receive): String = {
    objectMapper.writeValueAsString(r)
  }

  private def buildGroupMessage(message: Message, receive: Receive, gid: Int): Unit = {
    var receiveArchive: Receive = receive.copy(id = gid)
    val group = userService.findGroupById(gid)
    if (group == null) return
    //找到群组id里面的所有用户
    val users: util.List[User] = userService.findUserByGroupId(gid)
    //过滤掉本身的uid
    users.asScala
      .filter(_.id != message.mine.id)
      .foreach { user =>
        //是否在线
        if (actorRefSessions.containsKey(user.id)) {
          val actorRef = actorRefSessions.get(user.id)
          receiveArchive = receiveArchive.copy(status = 1)
          sendMessage(receiveStr(receiveArchive), actorRef)
        }
      }
    userService.saveMessage(receiveArchive)
  }

  /** 同意添加成员
    * 群解散后，申请和拒绝已经修改等都需要处理，这里暂时没有考虑
    *
    * @param msg
    */
  def agreeAddGroup(msg: Message): Unit = {
    log.debug(s"同意入群消息 => [msg = $msg]")
    val agree = objectMapper.readValue[Protocols.AddRefuseMessage](msg.msg)
    agree.messageBoxId.synchronized {
      val ret = userService.addGroupMember(agree.groupId, agree.toUid, agree.messageBoxId)
      if (!ret) return
      val groupList = userService.findGroupById(agree.groupId)
      // 通知加群成功
      val actor = actorRefSessions.get(agree.toUid)
      if (actor != null) {
        val message = Message(
          `type` = ImProtocol.agreeAddGroup.stringify,
          mine = agree.mine,
          to = null,
          msg = Jackson.mapper.writeValueAsString(groupList)
        )
        sendMessage(objectMapper.writeValueAsString(message), actor)
      }
    }
  }

  /** 拒绝添加群
    *
    * @param msg
    */
  def refuseAddGroup(msg: Message): Unit = {
    log.debug(s"拒绝入群消息 => [msg = $msg]")
    val refuse = objectMapper.readValue[Protocols.AddRefuseMessage](msg.msg)
    refuse.messageBoxId.synchronized {
      userService.updateAddMessage(refuse.messageBoxId, 2)
      val actor = actorRefSessions.get(refuse.toUid)
      if (actor != null) {
        val result = new util.HashMap[String, String]()
        result.put("type", "refuseAddGroup")
        result.put("username", refuse.mine.username)
        sendMessage(objectMapper.writeValueAsString(result), actor)
      }
    }
  }

  /** 拒绝加群
    *
    * @param messageBoxId
    * @param user
    * @param to
    */
  def refuseAddFriend(messageBoxId: Int, user: User, to: Int): Boolean = {
    messageBoxId.synchronized {
      val actor = actorRefSessions.get(to)
      if (actor != null) {
        val result = new util.HashMap[String, String]()
        result.put("type", "refuseAddFriend")
        result.put("username", user.username)
        sendMessage(objectMapper.writeValueAsString(result), actor)
      }
      userService.updateAddMessage(messageBoxId, 2)
    }
  }

  /** 群主删除群，在线用户收到群解散消息
    *
    * @param master
    * @param gid
    * @param uid
    */
  def deleteGroup(master: User, groupname: String, gid: Int, uid: Int): Unit = {
    gid.synchronized {
      val result = new util.HashMap[String, String]
      val actor = actorRefSessions.get(uid)
      if (actor != null && uid != master.id) {
        result.put("type", "deleteGroup")
        result.put("username", master.username)
        result.put("uid", master.id + "")
        result.put("groupname", groupname)
        result.put("gid", gid + "")
        sendMessage(objectMapper.writeValueAsString(result), actor)
      }
    }
  }

  /** 通知对方删除好友
    *
    * @param uId      我的id
    * @param friendId 对方Id
    */
  def removeFriend(uId: Int, friendId: Int): Unit =
    uId.synchronized {
      log.debug(s"删除好友通知消息 => [uId = $uId, friendId = $friendId ]")
      //对方是否在线，在线则处理，不在线则不处理
      val result = new util.HashMap[String, String]
      val actor = actorRefSessions.get(friendId)
      if (actor != null) {
        result.put("type", Protocols.ImProtocol.delFriend.stringify)
        result.put("uId", uId + "")
        sendMessage(objectMapper.writeValueAsString(result), actor)
      }
    }

  /** 添加群组
    *
    * @param uId
    * @param message
    */
  def addGroup(uId: Int, message: Message): Unit =
    uId.synchronized {
      log.debug(s"加群消息 => [uId = $uId, msg = $message ]")
      val mine = message.mine
      val to = message.to
      val t = objectMapper.readValue[Protocols.Group](message.msg)
      userService.saveAddMessage(
        AddMessage(
          fromUid = mine.id,
          toUid = to.id,
          groupId = t.groupId,
          remark = t.remark,
          `type` = 1,
          time = DateUtil.getDateTime
        )
      )
      val result = new util.HashMap[String, String]
      val actorRef = actorRefSessions.get(to.id)
      if (actorRef != null) {
        result.put("type", Protocols.ImProtocol.addGroup.stringify)
        sendMessage(objectMapper.writeValueAsString(result), actorRef)
      }
    }

  /** 添加好友
    *
    * @param uId
    * @param message
    */
  def addFriend(uId: Int, message: Message): Unit =
    uId.synchronized {
      log.debug(s"加好友消息 => [uId = $uId, msg = $message ]")
      val mine = message.mine
      val add = objectMapper.readValue[Add](message.msg)
      val addMessageCopy = AddMessage(
        fromUid = mine.id,
        toUid = message.to.id,
        groupId = add.groupId,
        remark = add.remark,
        `type` = add.`type`,
        time = DateUtil.getDateTime
      )
      userService.saveAddMessage(addMessageCopy)
      val result = new util.HashMap[String, String]
      //如果对方在线，则推送给对方
      val actorRef = actorRefSessions.get(message.to.id)
      if (actorRef != null) {
        result.put("type", Protocols.ImProtocol.addFriend.stringify)
        sendMessage(
          objectMapper.writeValueAsString(result),
          actorRef = actorRef
        )
      }
    }

  /** 统计离线消息数量
    *
    * @param uId
    * @return HashMap[String, String]
    */
  def countUnHandMessage(uId: Int): util.HashMap[String, String] =
    uId.synchronized {
      log.debug(s"离线消息统计 => [uId = $uId]")
      val count = userService.countUnHandMessage(uId, 0)
      log.info("count = " + count)
      val result = new util.HashMap[String, String]
      result.put("type", Protocols.ImProtocol.unHandMessage.stringify)
      result.put("count", count + "")
      result
    }

  /** 检测某个用户的离线或者在线
    *
    * @param message
    * @return HashMap[String, String]
    */
  def checkOnline(message: Message): util.HashMap[String, String] =
    message.to.id.synchronized {
      log.debug(s"检测在线状态 => [msg = ${message.to.toString}]")
      val uids = redisService.getSets(SystemConstant.ONLINE_USER)
      val result = new util.HashMap[String, String]
      result.put("type", Protocols.ImProtocol.checkOnline.stringify)
      if (uids.contains(message.to.id.toString))
        result.put("status", SystemConstant.status.ONLINE_DESC)
      else result.put("status", SystemConstant.status.HIDE_DESC)
      result
    }

  /** 发送消息
    *
    * @param message
    * @param actorRef
    */
  def sendMessage(message: String, actorRef: ActorRef): Unit =
    synchronized {
      actorRef ! message
    }

  /** 封装返回消息格式
    *
    * @param message
    * @return Receive
    */
  private def getReceive(message: Message): Receive = {
    val mine = message.mine
    val to = message.to
    Receive(
      id = mine.id,
      username = mine.username,
      avatar = mine.avatar,
      `type` = to.`type`,
      content = mine.content,
      cid = 0,
      mine = false,
      fromid = mine.id,
      timestamp = DateUtil.getLongDateTime,
      status = 0,
      toid = to.id
    )
  }

  /** 用户在线切换状态
    *
    * @param uId    用户id
    * @param status 状态
    */
  def changeOnline(uId: Int, status: String): Boolean =
    uId.synchronized {
      val isOnline = SystemConstant.status.ONLINE.equals(status)
      log.debug(s"更改在线状态 => [uId = $uId, status = $status]")
      if (isOnline) redisService.setSet(SystemConstant.ONLINE_USER, uId + "")
      else redisService.removeSetValue(SystemConstant.ONLINE_USER, uId + "")
      // 向我的所有在线好友发送广播消息，告知我的状态变更，否则只能再次打聊天开窗口时变更,todo 异步发送
      userService
        .findFriendGroupsById(uId)
        .asScala
        .filter(l => l != null && l.list != null && !l.list.isEmpty)
        .foreach { fl =>
          fl.list.asScala.foreach(u => {
            val fu = redisService.getSets(SystemConstant.ONLINE_USER).contains(u.id.toString)
            val actorRef = actorRefSessions.get(u.id)
            if (fu && actorRef != null) {
              val msg = Jackson.mapper.writeValueAsString(
                Map(
                  "id" -> (uId + ""), //对好友而已，好友的好友就是我
                  "type" -> Protocols.ImProtocol.checkOnline.stringify,
                  "status" -> (if (isOnline) SystemConstant.status.ONLINE_DESC
                               else SystemConstant.status.HIDE_DESC)
                )
              )
              sendMessage(msg, actorRef)
            }
          })
        }
      userService.updateUserStatus(User(uId, status))
    }

  /** 已读，先简单实现，打开对话框时，与该好友和群的所有信息置为已读
    *
    * @param message
    */
  def readOfflineMessage(message: Message): Unit = {
    message.mine.id.synchronized {
      if (userService.findOffLineMessage(message.mine.id, 0).asScala.toList.nonEmpty) {
        if (message.to.`type` == SystemConstant.GROUP_TYPE) {
          // 我所有的群中有未读的消息吗
          userService.readGroupMessage(message.mine.id, message.mine.id)
        } else {
          userService.readFriendMessage(message.mine.id, message.to.id)
        }
      }
    }
  }

  //用于统计实时在线的人数，根据ConcurrentHashMap特性，该人数不会很准确
  //重连之后会重新加入进来，但与Redis还是有差异
  @volatile def getConnections: Int = actorRefSessions.size()
}
