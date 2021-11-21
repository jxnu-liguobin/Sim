package io.github.dreamylost.websocket

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.settings.ServerSettings
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import io.github.dreamylost.constant.SystemConstant
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.Future
import scala.io.StdIn

/**
  * akka-http websocket server
  *
 * BUG：前端初始化总会抛一个异常
  *
 * @author 梦境迷离
  * @version 1.0,2020/1/22
  */
@Component
class WebSocketServer @Autowired() (redisService: RedisService, akkaService: WebSocketProvider) {

  import ActorCommon._
  import Directives._

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[WebSocketServer])
  private val host = ConfigFactory.load("application.conf").getString("akka-http-server.host")
  private val port = ConfigFactory.load("application.conf").getInt("akka-http-server.port")

  private val imServerSettings = {
    //自定义保持活动数据有效负载
    val defaultSettings = ServerSettings(system)
    val pingCounter = new AtomicInteger()
    val imWebsocketSettings = defaultSettings.websocketSettings.withPeriodicKeepAliveData(() =>
      ByteString(s"debug-ping-${pingCounter.incrementAndGet()}")
    )
    defaultSettings.withWebsocketSettings(imWebsocketSettings)
  }

  private val imRoute = {
    path("websocket") {
      get {
        parameters("uid".as[Int]) { uid =>
          LOGGER.info(s"新连接加入 => [userId = $uid]")
          redisService.setSet(SystemConstant.ONLINE_USER, uid + "")
          //          akkaService.userStatusChangeByServer(uid, "online")
          handleWebSocketMessages(akkaService.openConnection(uid))
        }
      }
    }
  }

  def startUp(): Unit = {
    val bindingFuture = Http().bindAndHandle(imRoute, host, port, settings = imServerSettings)
    bindingFuture.failed.foreach { ex =>
      LOGGER.error(s"Failed to bind to $host:$port!")
    }
    LOGGER.info(
      """
        | __      __      ___.     _________              __           __      _________
        |/  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_   /   _____/ ______________  __ ___________
        |\   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\  \_____  \_/ __ \_  __ \  \/ // __ \_  __ \
        | \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |    /        \  ___/|  | \/\   /\  ___/|  | \/
        |  \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__|   /_______  /\___  >__|    \_/  \___  >__|
        |       \/       \/    \/        \/            \/     \/    \/               \/     \/                 \/
        |""".stripMargin
    )
    LOGGER.info(s"websocket listener on [$host:$port]")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }

  Future {
    startUp()
  }
}
