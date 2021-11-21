package io.github.dreamylost.websocket

import akka.actor.ActorRef
import com.google.gson.Gson
import io.github.dreamylost.Application
import io.github.dreamylost.constant.SystemConstant
import io.github.dreamylost.model.domain.Add
import io.github.dreamylost.model.domain.Receive
import io.github.dreamylost.model.entity.AddMessage
import io.github.dreamylost.model.entity.Message
import io.github.dreamylost.model.entity.User
import io.github.dreamylost.service.UserService
import io.github.dreamylost.util.DateUtil
import io.github.dreamylost.websocket.Domain.AgreeAddGroup
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._

/**
  * WebSocket 单例
  *
 * @since 2020年01月23日
  * @author 梦境迷离
  * @version 1.2
  */
object WebSocketService {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(WebSocketService.getClass)
  private final lazy val application = Application.getApplicationContext
  final val actorRefSessions = new ConcurrentHashMap[Integer, ActorRef]
  private lazy val userService: UserService = application.getBean(classOf[UserService])
  private lazy val redisService: RedisService = application.getBean(classOf[RedisService])

  private lazy final val gson: Gson = new Gson

  /**
    * 发送消息
    *
   * @param message
    */
  def sendMessage(message: Message): Unit =
    synchronized {
      LOGGER.debug(s"好友消息或群消息 => [msg = $message]")
      //封装返回消息格式
      val gid = message.to.id
      val receive = WebSocketService.getReceive(message)
      val key: Integer = message.to.id
      val strMsg = () => {
        gson.toJson(receive).replaceAll("Type", "type")
      }
      //聊天类型，可能来自朋友或群组
      if ("friend" == message.to.Type) {
        //是否在线
        val receiveMsg = if (WebSocketService.actorRefSessions.containsKey(key)) {
          val actorRef = WebSocketService.actorRefSessions.get(key)
          WebSocketService.sendMessage(strMsg(), actorRef)
          receive.copy(status = 1)
        } else receive
        //保存为离线消息,默认为离线消息
        userService.saveMessage(receiveMsg)
      } else {
        val receiveMsg = receive.copy(id = gid)
        //找到群组id里面的所有用户
        val users: util.List[User] = userService.findUserByGroupId(gid)
        //过滤掉本身的uid
        users.asScala
          .filter(_.id != message.mine.id)
          .foreach { user =>
            {
              //是否在线
              val receiveMsgCopy = if (WebSocketService.actorRefSessions.containsKey(user.id)) {
                val actorRef = WebSocketService.actorRefSessions.get(user.id)
                WebSocketService.sendMessage(strMsg(), actorRef)
                receiveMsg.copy(status = 1)
              } else {
                receiveMsg.copy(id = key)
              }
              //TODO 全部保存为离线消息?
              userService.saveMessage(receiveMsgCopy)
            }
          }
      }
    }

  /**
    * 同意添加成员
    *
   * @param msg
    */
  def agreeAddGroup(msg: Message): Unit = {
    LOGGER.debug(s"同意入群消息 => [msg = $msg]")
    val agree = gson.fromJson(msg.msg, classOf[AgreeAddGroup])
    userService.addGroupMember(agree.groupId, agree.toUid, agree.messageBoxId)
  }

  /**
    * 拒绝添加群
    *
   * @param msg
    */
  def refuseAddGroup(msg: Message): Unit = {
    LOGGER.debug(s"拒绝入群消息 => [msg = $msg]")
    val refuse = gson.fromJson(msg.msg, classOf[Domain.AgreeAddGroup])
    userService.updateAddMessage(refuse.messageBoxId, 2)
  }

  /**
    * 通知对方删除好友
    *
   * @param uId      我的id
    * @param friendId 对方Id
    */
  def removeFriend(uId: Integer, friendId: Integer) =
    synchronized {
      LOGGER.debug(s"删除好友通知消息 => [uId = $uId, friendId = $friendId ]")
      //对方是否在线，在线则处理，不在线则不处理
      val result = new util.HashMap[String, String]
      if (actorRefSessions.get(friendId) != null) {
        result.put("type", "delFriend")
        result.put("uId", uId + "")
        WebSocketService.sendMessage(gson.toJson(result), actorRefSessions.get(friendId))
      }
    }

  /**
    * 添加群组
    *
   * @param uId
    * @param message
    */
  def addGroup(uId: Integer, message: Message): Unit =
    synchronized {
      LOGGER.debug(s"加群消息 => [uId = $uId, msg = $message ]")
      val mine = message.mine
      val to = message.to
      val t = gson.fromJson(message.msg, classOf[Domain.Group])
      userService.saveAddMessage(
        AddMessage(
          fromUid = mine.id,
          toUid = to.id,
          groupId = t.groupId,
          remark = t.remark,
          Type = 1,
          time = DateUtil.getDateTime
        )
      )
      val result = new util.HashMap[String, String]
      if (actorRefSessions.get(to.id) != null) {
        result.put("type", "addGroup")
        sendMessage(gson.toJson(result), actorRefSessions.get(to.id))
      }
    }

  /**
    * 添加好友
    *
   * @param uId
    * @param message
    */
  def addFriend(uId: Int, message: Message): Unit =
    synchronized {
      LOGGER.debug(s"加好友消息 => [uId = $uId, msg = $message ]")
      val mine = message.mine
      val add = gson.fromJson(message.msg, classOf[Add])
      val addMessageCopy = AddMessage(
        fromUid = mine.id,
        toUid = message.to.id,
        groupId = add.groupId,
        remark = add.remark,
        Type = add.Type,
        time = DateUtil.getDateTime
      )
      userService.saveAddMessage(addMessageCopy)
      val result = new util.HashMap[String, String]
      //如果对方在线，则推送给对方
      if (actorRefSessions.get(message.to.id) != null) {
        result.put("type", "addFriend")
        sendMessage(gson.toJson(result), actorRef = actorRefSessions.get(message.to.id))
      }
    }

  /**
    * 统计离线消息数量
    *
   * @param uId
    * @return HashMap[String, String]
    */
  def countUnHandMessage(uId: Int): util.HashMap[String, String] =
    synchronized {
      LOGGER.debug(s"离线消息统计 => [uId = $uId]")
      val count = userService.countUnHandMessage(uId, 0)
      LOGGER.info("count = " + count)
      val result = new util.HashMap[String, String]
      result.put("type", "unHandMessage")
      result.put("count", count + "")
      result
    }

  /**
    * 检测某个用户的离线或者在线
    *
   * @param message
    * @return HashMap[String, String]
    */
  def checkOnline(message: Message, actorRef: ActorRef): util.HashMap[String, String] =
    synchronized {
      LOGGER.debug(s"检测在线状态 => [msg = ${message.to.toString}]")
      val uids = redisService.getSets(SystemConstant.ONLINE_USER)
      val result = new util.HashMap[String, String]
      result.put("type", "checkOnline")
      if (uids.contains(message.to.id.toString)) result.put("status", "在线")
      else result.put("status", "离线")
      result
    }

  /**
    * 发送消息
    *
   * @param message
    * @param actorRef
    */
  def sendMessage(message: String, actorRef: ActorRef): Unit =
    synchronized {
      actorRef ! message
    }

  /**
    * 封装返回消息格式
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
      Type = to.Type,
      content = mine.content,
      cid = 0,
      mine = false,
      fromid = mine.id,
      timestamp = DateUtil.getLongDateTime,
      status = 0,
      toid = to.id
    )
  }

  /**
    * 用户在线切换状态
    *
   * @param uId    用户id
    * @param status 状态
    */
  def changeOnline(uId: Integer, status: String): Boolean =
    synchronized {
      LOGGER.debug(s"检测在线状态 => [uId = $uId, status = $status]")
      if ("online".equals(status)) redisService.setSet(SystemConstant.ONLINE_USER, uId + "")
      else redisService.removeSetValue(SystemConstant.ONLINE_USER, uId + "")
      userService.updateUserStatus(User(uId, status))
    }

  /**
    * 已读，先简单实现，打开对话框时，与该好友的所有信息置为已读
    *
   * @param message
    */
  def readOfflineMessage(message: Message): Unit = {
    synchronized {
      userService.readFriendMessage(message.mine.id, message.to.id)
    }
  }

  //用于统计实时在线的人数，根据ConcurrentHashMap特性，该人数不会很准确
  //重连之后会重新加入进来，但与Redis还是有差异
  @volatile def getConnections = actorRefSessions.size()
}
