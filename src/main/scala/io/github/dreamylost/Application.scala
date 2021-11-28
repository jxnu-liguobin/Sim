package io.github.dreamylost

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan

/** IDEA添加 vm参数 -Dspring.output.ansi.enabled=ALWAYS  打印彩色控制台
 */
@SpringBootApplication
@EntityScan(Array("io.github.dreamylost.model"))
@MapperScan(Array("io.github.dreamylost.repository"))
class ApplicationConfig

object Application extends App {

  SpringApplication.run(classOf[ApplicationConfig])

}
