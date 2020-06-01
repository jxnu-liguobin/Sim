package cn.edu.layim.websocket

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Materializer

/**
  *
 * @author liguobin@growingio.com
  * @version 1.0,2020/6/1
  */
object ActorCommon {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

}
