package io.github.dreamylost.websocket

import akka.Done
import akka.NotUsed
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Status
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import io.github.dreamylost.constant.SystemConstant
import io.github.dreamylost.log
import io.github.dreamylost.logs.LogType
import io.github.dreamylost.websocket.Protocols._
import io.github.dreamylost.websocket.SpringExtension.SpringExtProvider
import org.reactivestreams.Publisher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

/** 基于 akka stream 和 akka http的 websocket
  *
  * @since 2020年01月27日
  * @author 梦境迷离
  * @version 1.2
  */
@Component
@DependsOn(Array("redisService"))
@log(logType = LogType.Slf4j)
class WebSocketProvider @Autowired() (redisService: RedisService, wsService: WebSocketService)(
    implicit system: ActorSystem
) {

  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()

  private final lazy val wsConnections = wsService.actorRefSessions
  private lazy val msgActor: ActorRef =
    system.actorOf(SpringExtProvider.get(system).props(ActorNames.MESSAGE_HANDLE_ACTOR))
  private lazy val jobActor: ActorRef =
    system.actorOf(SpringExtProvider.get(system).props(ActorNames.SCHEDULE_JOB_ACTOR))
  private lazy val userStatusActor: ActorRef =
    system.actorOf(SpringExtProvider.get(system).props(ActorNames.USER_STATUS_CHANGE_ACTOR))

  //重连是3秒
  system.scheduler.schedule(5000 milliseconds, 10000 milliseconds, jobActor, OnlineUserMessage)

  /** 处理连接与消息处理
    *
    * @param uId
    * @return
    */
  def openConnection(uId: Int): Flow[Message, Message, NotUsed] = {
    redisService.setSet(SystemConstant.ONLINE_USER, uId + "")

    //刷新重连
    //closeConnection(uId)
    val (actorRef: ActorRef, publisher: Publisher[TextMessage.Strict]) = {
      Source
        .actorRef[String](16, OverflowStrategy.fail)
        .map(TextMessage.Strict)
        .toMat(Sink.asPublisher(false))(Keep.both)
        .run()
    }
    val out = Source.fromPublisher(publisher)
    val in: Sink[Message, Unit] = {
      Flow[Message]
        .watchTermination()((_, ft) => ft.foreach { _ => closeConnection(uId) })
        .mapConcat {
          case TextMessage.Strict(message) =>
            msgActor ! TransmitMessage(uId, message, actorRef)
            Nil
          case _ => Nil
        }
        .to(Sink.ignore)
    }

    log.info(s"Opening websocket connection => [uid = $uId]")
    wsConnections.put(uId, actorRef)
    Flow.fromSinkAndSource(in, out)
  }

  /** 关闭websocket
    *
    * @param id
    */
  def closeConnection(id: Integer) = {
    wsConnections.asScala.get(id).foreach { ar =>
      log.info(s"Closing websocket connection => [id = $id]")
      wsConnections.remove(id)
      redisService.removeSetValue(SystemConstant.ONLINE_USER, id + "")
      //      userStatusChangeByServer(id, "hide")
      ar ! Status.Success(Done)
    }
  }

  //
  //  def userStatusChangeByServer(uId: Int, status: String): Unit = {
  //    userStatusActor ! UserStatusChange(uId, status)
  //  }
}
