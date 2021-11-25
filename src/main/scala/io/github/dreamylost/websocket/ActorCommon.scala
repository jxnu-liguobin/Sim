package io.github.dreamylost.websocket

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.Materializer

/** @author 梦境迷离
  * @version 1.0,2020/6/1
  */
object ActorCommon {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

}
