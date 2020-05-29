package cn.edu.layim.websocket

import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorRef
import cn.edu.layim.Application
import cn.edu.layim.constant.SystemConstant
import cn.edu.layim.domain.Add
import cn.edu.layim.domain.Receive
import cn.edu.layim.entity.AddMessage
import cn.edu.layim.entity.Message
import cn.edu.layim.entity.User
import cn.edu.layim.service.RedisService
import cn.edu.layim.service.UserService
import cn.edu.layim.util.DateUtil
import cn.edu.layim.websocket.Domain.AgreeAddGroup
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * WebSocket 单例
  *
 * @date 2020年01月23日
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
   * @param  message
    */
  def sendMessage(message: Message): Unit =
    synchronized {
      LOGGER.debug(s"好友消息或群消息 => [msg = $message]");
      //封装返回消息格式
      val gid = message.getTo.getId
      val receive = WebSocketService.getReceive(message)
      val key: Integer = message.getTo.getId
      val strMsg = () => {
        gson.toJson(receive).replaceAll("Type", "type")
      }
      //聊天类型，可能来自朋友或群组
      if ("friend" == message.getTo.getType) {
        //是否在线
        if (WebSocketService.actorRefSessions.containsKey(key)) {
          val actorRef = WebSocketService.actorRefSessions.get(key)
          receive.setStatus(1)
          WebSocketService.sendMessage(strMsg(), actorRef)
        }
        //保存为离线消息,默认为离线消息
        userService.saveMessage(receive)
      } else {
        receive.setId(gid)
        //找到群组id里面的所有用户
        val users: util.List[User] = userService.findUserByGroupId(gid)
        //过滤掉本身的uid
        users.asScala
          .filter(_.id != message.getMine.getId)
          .foreach { user =>
            {
              //是否在线
              if (WebSocketService.actorRefSessions.containsKey(user.getId)) {
                val actorRef = WebSocketService.actorRefSessions.get(user.getId)
                receive.setStatus(1)
                WebSocketService.sendMessage(strMsg(), actorRef)
              } else {
                receive.setId(key)
              }
            }
          }
        //保存为离线消息
        userService.saveMessage(receive)
      }
    }

  /**
    * 同意添加成员
    *
   * @param msg
    */
  def agreeAddGroup(msg: Message): Unit = {
    LOGGER.debug(s"同意入群消息 => [msg = $msg]");
    val agree = gson.fromJson(msg.getMsg, classOf[AgreeAddGroup])
    userService.addGroupMember(agree.groupId, agree.toUid, agree.messageBoxId)
  }

  /**
    * 拒绝添加群
    *
   * @param msg
    */
  def refuseAddGroup(msg: Message): Unit = {
    LOGGER.debug(s"拒绝入群消息 => [msg = $msg]");
    val refuse = gson.fromJson(msg.getMsg, classOf[Domain.AgreeAddGroup])
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
      LOGGER.debug(s"删除好友通知消息 => [uId = $uId, friendId = $friendId ]");
      //对方是否在线，在线则处理，不在线则不处理
      val result = new util.HashMap[String, String]
      if (actorRefSessions.get(friendId) != null) {
        result.put("type", "delFriend");
        result.put("uId", uId + "");
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
      LOGGER.debug(s"加群消息 => [uId = $uId, msg = $message ]");
      val addMessage = new AddMessage
      val mine = message.getMine
      val to = message.getTo
      val t = gson.fromJson(message.getMsg, classOf[Domain.Group])
      addMessage.setFromUid(mine.getId)
      addMessage.setToUid(to.getId)
      addMessage.setTime(DateUtil.getDateTime)
      addMessage.setGroupId(t.groupId)
      addMessage.setRemark(t.remark)
      addMessage.setType(1)
      userService.saveAddMessage(addMessage)
      val result = new util.HashMap[String, String]
      if (actorRefSessions.get(to.getId) != null) {
        result.put("type", "addGroup");
        sendMessage(gson.toJson(result), actorRefSessions.get(to.getId))
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
      LOGGER.debug(s"加好友消息 => [uId = $uId, msg = $message ]");
      val mine = message.getMine
      val addMessage = new AddMessage
      addMessage.setFromUid(mine.getId)
      addMessage.setTime(DateUtil.getDateTime)
      addMessage.setToUid(message.getTo.getId)
      val add = gson.fromJson(message.getMsg, classOf[Add])
      addMessage.setRemark(add.getRemark)
      addMessage.setType(add.getType)
      addMessage.setGroupId(add.getGroupId)
      userService.saveAddMessage(addMessage)
      val result = new util.HashMap[String, String]
      //如果对方在线，则推送给对方
      if (actorRefSessions.get(message.getTo.getId) != null) {
        result.put("type", "addFriend")
        sendMessage(gson.toJson(result), actorRefSessions.get(message.getTo.getId))
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
      LOGGER.debug(s"离线消息统计 => [uId = $uId]");
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
      LOGGER.debug(s"检测在线状态 => [msg = ${message.getTo.toString}]")
      val uids = redisService.getSets(SystemConstant.ONLINE_USER)
      val result = new util.HashMap[String, String]
      result.put("type", "checkOnline")
      if (uids.contains(message.getTo.getId.toString)) result.put("status", "在线")
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
    val mine = message.getMine
    val to = message.getTo
    val receive = new Receive
    receive.setId(mine.getId)
    receive.setFromid(mine.getId)
    receive.setToid(to.getId)
    receive.setUsername(mine.getUsername)
    receive.setType(to.getType)
    receive.setAvatar(mine.getAvatar)
    receive.setContent(mine.getContent)
    receive.setTimestamp(DateUtil.getLongDateTime)
    receive
  }

  /**
    * 用户在线切换状态
    *
   * @param uId    用户id
    * @param status 状态
    */
  def changeOnline(uId: Integer, status: String) =
    synchronized {
      LOGGER.debug(s"检测在线状态 => [uId = $uId, status = $status]")
      if ("online".equals(status)) redisService.setSet(SystemConstant.ONLINE_USER, uId + "")
      else redisService.removeSetValue(SystemConstant.ONLINE_USER, uId + "")
    }

  //用于统计实时在线的人数，根据ConcurrentHashMap特性，该人数不会很准确
  //重连之后会重新加入进来，但与Redis还是有差异
  @volatile def getConnections = actorRefSessions.size()

}
