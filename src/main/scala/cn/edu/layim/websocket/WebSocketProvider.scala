package cn.edu.layim.websocket

import akka.actor.{ ActorRef, ActorSystem, Props, Status }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import akka.stream.{ ActorMaterializer, Materializer, OverflowStrategy }
import akka.{ Done, NotUsed }
import cn.edu.layim.actor.ActorMessage._
import cn.edu.layim.actor.{ MessageHandleActor, ScheduleJobActor }
import cn.edu.layim.constant.SystemConstant
import cn.edu.layim.service.RedisService
import org.reactivestreams.Publisher
import org.slf4j.{ Logger, LoggerFactory }
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * 基于 akka stream 和 akka http的 websocket
 *
 * @date 2020年01月27日
 * @author 梦境迷离
 * @version 1.2
 */
@Component
class WebSocketProvider @Autowired()(redisService: RedisService) {
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  private final lazy val log: Logger = LoggerFactory.getLogger(classOf[WebSocketProvider])
  private final lazy val wsConnections = WebSocketService.actorRefSessions
  private lazy val msgActor = system.actorOf(Props(classOf[MessageHandleActor]))
  private lazy val jobActor = system.actorOf(Props(classOf[ScheduleJobActor]))

  //重连是3秒
  system.scheduler.schedule(5000 milliseconds, 500 milliseconds, jobActor, OnlineUserMessage)

  /**
   * 处理连接与消息处理
   *
   * @param uId
   * @return
   */
  def openConnection(uId: Integer): Flow[Message, Message, NotUsed] = {
    //刷新重连
    closeConnection(uId)
    val (actorRef: ActorRef, publisher: Publisher[TextMessage.Strict]) = {
      Source.actorRef[String](16, OverflowStrategy.fail).map(TextMessage.Strict).toMat(Sink.asPublisher(false))(Keep.both).run()
    }
    val out = Source.fromPublisher(publisher)
    val in: Sink[Message, Unit] = {
      Flow[Message].watchTermination()((_, ft) => ft.foreach { _ => closeConnection(uId) }).mapConcat {
        case TextMessage.Strict(message) =>
          msgActor ! TransmitMessage(uId, message, actorRef)
          Nil
        case _ => Nil
      }.to(Sink.ignore)
    }

    log.info(s"Opening websocket connection => [uid = $uId]")
    wsConnections.put(uId, actorRef)
    Flow.fromSinkAndSource(in, out)
  }

  /**
   * 关闭websocket
   *
   * @param id
   */
  def closeConnection(id: Integer) = {
    wsConnections.asScala.get(id).foreach { ar =>
      log.info(s"Closing websocket connection => [id = $id]")
      wsConnections.remove(id)
      redisService.removeSetValue(SystemConstant.ONLINE_USER, id + "")
      ar ! Status.Success(Done)
    }
  }
}