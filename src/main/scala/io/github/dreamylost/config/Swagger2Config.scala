package io.github.dreamylost.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

/** swagger2
  *
  * @author 梦境迷离
  * @since 2018-10-08
  */
@Configuration
@EnableSwagger2
class Swagger2Config {

  /** swagger2的配置文件，这里可以配置swagger2的一些基本的内容，比如扫描的包等等
    */
  @Bean
  def createRestApi(): Docket =
    new Docket(DocumentationType.SWAGGER_2)
      .apiInfo(apiInfo())
      .select()
      .apis(RequestHandlerSelectors.basePackage("io.github.dreamylost.controller"))
      .paths(PathSelectors.any())
      .build()

  private lazy val apiInfo: () => ApiInfo = () =>
    new ApiInfoBuilder()
      // 页面标题
      .title("LayIM")
      // 创建人
      .description("梦境迷离：https://github.com/jxnu-liguobin")
      .termsOfServiceUrl("https://github.com/jxnu-liguobin")
      // 创建人
      .contact("梦境迷离")
      // 版本号
      .version("1.2")
      .build()

}
