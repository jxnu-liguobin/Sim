package cn.edu.layim

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer
import org.springframework.context.ApplicationContext
import springfox.documentation.swagger2.annotations.EnableSwagger2

import scala.beans.BeanProperty

/**
  * IDEA添加 vm参数 -Dspring.output.ansi.enabled=ALWAYS  打印彩色控制台
  */
@SpringBootApplication
@EnableSwagger2
@EntityScan(Array("cn.edu.layim.entity"))
@MapperScan(Array("cn.edu.layim.repository"))
class Config

object Application extends SpringBootServletInitializer {

    @BeanProperty var applicationContext: ApplicationContext = null

    def main(args: Array[String]) = applicationContext = SpringApplication.run(classOf[Config])

    override protected def configure(builder: SpringApplicationBuilder) = builder.sources(Application)
}
