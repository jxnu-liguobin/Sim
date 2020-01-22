package cn.edu.layim.websocket

import cn.edu.layim.Application
import cn.edu.layim.constant.SystemConstant
import cn.edu.layim.entity.Message
import cn.edu.layim.service.RedisService
import cn.edu.layim.util.WebSocketUtil
import com.google.gson.Gson
import javax.websocket._
import javax.websocket.server.{ PathParam, ServerEndpoint }
import org.slf4j.{ Logger, LoggerFactory }
import org.springframework.stereotype.Component

/**
 * websocket服务器处理消息
 *
 * @date 2018年9月8日
 * @author 梦境迷离
 */
@ServerEndpoint(value = "/websocket/{uid}")
@Component
class WebSocket {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[WebSocket])

  private lazy val redisService: RedisService = Application.getApplicationContext.getBean(classOf[RedisService])

  private final lazy val gson: Gson = new Gson

  private var uid: Integer = _

  /**
   * 服务器接收到消息调用
   *
   * @param message 消息体
   * @param session
   */
  @OnMessage
  def onMessage(message: String, session: Session) {
    val mess = gson.fromJson(message.replaceAll("type", "Type"), classOf[Message])
    LOGGER.info("来自客户端的消息: " + mess)
    mess.getType match {
      case "message" => {
        WebSocketUtil.sendMessage(mess)
      }
      case "checkOnline" => {
        val result = WebSocketUtil.checkOnline(mess, session)
        WebSocketUtil.sendMessage(gson.toJson(result), session)
      }
      case "addGroup" => {
        WebSocketUtil.addGroup(uid, mess)
      }
      case "changOnline" => {
        WebSocketUtil.changeOnline(uid, mess.getMsg)
      }
      case "addFriend" => {
        WebSocketUtil.addFriend(uid, mess)
      }
      case "agreeAddFriend" => {
        if (WebSocketUtil.getSessions.get(mess.getTo.getId) != null) {
          WebSocketUtil.sendMessage(message, WebSocketUtil.getSessions.get(mess.getTo.getId))
        }
      }
      case "agreeAddGroup" => {
        WebSocketUtil.agreeAddGroup(mess)
      }
      case "refuseAddGroup" => {
        WebSocketUtil.refuseAddGroup(mess);
      }
      case "unHandMessage" => {
        val result = WebSocketUtil.countUnHandMessage(uid)
        WebSocketUtil.sendMessage(gson.toJson(result), session)
      }
      case "delFriend" => {
        WebSocketUtil.removeFriend(uid, mess.getTo.getId)
      }
      case _ => {
        LOGGER.info("No Mapping Message!")
      }
    }
  }

  /**
   * 首次创建链接
   *
   * @param session
   * @param uid
   */
  @OnOpen
  def onOpen(session: Session, @PathParam("uid") uid: Integer): Unit = {
    this.uid = uid
    WebSocketUtil.sessions.put(uid, session)
    LOGGER.info("userId = " + uid + ",sessionId = " + session.getId + ",新连接加入!")
    redisService.setSet(SystemConstant.ONLINE_USER, uid + "")
  }

  /**
   * 链接关闭调用
   *
   * @param session
   */
  @OnClose
  def onClose(session: Session) = {
    LOGGER.info("userId = " + uid + ",sessionId = " + session.getId + "断开连接!")
    WebSocketUtil.getSessions().remove(uid)
    redisService.removeSetValue(SystemConstant.ONLINE_USER, uid + "")
  }

  /**
   * 服务器发送错误调用
   *
   * @param session
   * @param error
   */
  @OnError
  def onError(session: Session, error: Throwable) = {
    LOGGER.info(session.getId + " 发生错误" + error.printStackTrace)
    onClose(session);
  }

}