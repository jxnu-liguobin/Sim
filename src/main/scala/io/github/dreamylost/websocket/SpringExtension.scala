package io.github.dreamylost.websocket

import akka.actor.AbstractExtensionId
import akka.actor.Actor
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.IndirectActorProducer
import akka.actor.Props
import io.github.dreamylost.websocket.SpringExtension.SpringExt
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import com.typesafe.config.Config

/** springboot集成akka actor
  *
  * @author 梦境迷离
  * @version 1.0,2021/11/25
  */
class SpringExtension extends AbstractExtensionId[SpringExtension.SpringExt] {

  override def createExtension(system: ExtendedActorSystem): SpringExt = new SpringExt

}

object SpringExtension {

  val SpringExtProvider = new SpringExtension

  class SpringExt extends Extension {

    private var applicationContext: ApplicationContext = _

    def initialize(applicationContext: ApplicationContext): Unit = {
      this.applicationContext = applicationContext
    }

    @inline def props(actorBeanName: String): Props = {
      Props.create(classOf[SpringActorProducer], applicationContext, actorBeanName)
    }
  }
}

class SpringActorProducer(val applicationContext: ApplicationContext, val actorBeanName: String)
    extends IndirectActorProducer {

  override def produce: Actor = applicationContext.getBean(actorBeanName).asInstanceOf[Actor]

  override def actorClass: Class[_ <: Actor] =
    applicationContext.getType(actorBeanName).asInstanceOf[Class[_ <: Actor]]
}

@Configuration
class AkkaConfig {

  @Autowired
  private var applicationContext: ApplicationContext = _

  @Bean
  def actorSystem: ActorSystem = {
    val actorSystem = ActorSystem.create("ActorSystem")
    SpringExtension.SpringExtProvider.get(actorSystem).initialize(applicationContext)
    actorSystem
  }

  @Bean def akkaConfiguration: Config = ConfigFactory.load
}
