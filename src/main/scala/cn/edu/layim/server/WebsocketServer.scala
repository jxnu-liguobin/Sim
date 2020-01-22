package cn.edu.layim.server


import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{ BinaryMessage, Message, TextMessage }
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

/**
 * akka-http websocket server
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/1/22
 */
object WebsocketServer extends App {

  println(
    """
      | __      __      ___.     _________              __           __      _________
      |/  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_   /   _____/ ______________  __ ___________
      |\   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\  \_____  \_/ __ \_  __ \  \/ // __ \_  __ \
      | \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |    /        \  ___/|  | \/\   /\  ___/|  | \/
      |  \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__|   /_______  /\___  >__|    \_/  \___  >__|
      |       \/       \/    \/        \/            \/     \/    \/               \/     \/                 \/
      |""".stripMargin)

  import Directives._

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val host = "localhost"
  val port = 8080

  val IMWebSocketService = {
    Flow[Message].mapConcat {
      case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
      case bm: BinaryMessage =>
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }
  }

  val IMServerSettings = {
    //自定义保持活动数据有效负载
    val defaultSettings = ServerSettings(system)
    val pingCounter = new AtomicInteger()
    val IMWebsocketSettings = defaultSettings.websocketSettings.
      withPeriodicKeepAliveData(() => ByteString(s"debug-ping-${pingCounter.incrementAndGet()}"))
    defaultSettings.withWebsocketSettings(IMWebsocketSettings)
  }

  //eg  ws://127.0.0.1:8080/websocket?uid=1
  val IMRoute = {
    path("websocket") {
      get {
        parameters("uid".as[String]) { uid =>
          println("当前有用户连接了 => [id = " + uid + "]")
          handleWebSocketMessages(IMWebSocketService)
        }
      }
    }
  }

  def startUp() {
    val bindingFuture = Http().bindAndHandle(IMRoute, host, port, settings = IMServerSettings)
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }

  startUp()
}


