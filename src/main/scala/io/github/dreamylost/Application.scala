package io.github.dreamylost

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer
import org.springframework.context.ApplicationContext

import scala.beans.BeanProperty

/** IDEA添加 vm参数 -Dspring.output.ansi.enabled=ALWAYS  打印彩色控制台
  */
@SpringBootApplication
@EntityScan(Array("io.github.dreamylost.model"))
@MapperScan(Array("io.github.dreamylost.repository"))
class ApplicationConfig

object Application extends SpringBootServletInitializer {

  @BeanProperty
  var applicationContext: ApplicationContext = _

  def main(args: Array[String]): Unit =
    applicationContext = SpringApplication.run(classOf[ApplicationConfig], args: _*)

  override protected def configure(builder: SpringApplicationBuilder): SpringApplicationBuilder =
    builder.sources(Application)

}
