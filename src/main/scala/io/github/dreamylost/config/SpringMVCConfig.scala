package io.github.dreamylost.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.dreamylost.util.Jackson
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.config.annotation.CorsRegistry

/** SpringMVC配置
  *
  * @since 2018年9月8日
  * @author 梦境迷离
  */
@Configuration
class SpringMVCConfig extends WebMvcConfigurerAdapter {

  //在SpringBoot2.0及Spring 5.0 WebMvcConfigurerAdapter已被废弃
  //    1.直接实现WebMvcConfigurer （官方推荐）
  //    2.直接继承WebMvcConfigurationSupport

  /** 重写addViewControllers方法配置默认主页
    *
    * @param registry
    */
  override def addViewControllers(registry: ViewControllerRegistry): Unit = {
    registry.addViewController("/").setViewName("forward:/index.html")
    registry.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
  }

  /** 注册拦截器
    *
    * @param registry
    */
  override def addInterceptors(registry: InterceptorRegistry): Unit = {
    // addPathPatterns 用于添加拦截规则，excludePathPatterns 用户排除拦截
    registry
      .addInterceptor(new SystemHandlerInterceptor)
      .excludePathPatterns("/")
      .excludePathPatterns("/*.html")
      .excludePathPatterns("/user/active/*") //别拦截激活URL
      .excludePathPatterns("/user/login")
      .excludePathPatterns("/user/register")
      .excludePathPatterns("/user/existEmail")
      .excludePathPatterns("/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**")
      .addPathPatterns("/**")
  }

  /** addResourceLocations是必须的，否则swagger被拦截
    *
    * @param registry
    */
  override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
    registry
      .addResourceHandler("swagger-ui.html")
      .addResourceLocations("classpath:/META-INF/resources/")
    registry
      .addResourceHandler("/webjars/**")
      .addResourceLocations("classpath:/META-INF/resources/webjars/")
  }

  @Bean
  @Primary
  @ConditionalOnMissingBean(Array(classOf[ObjectMapper]))
  def jacksonObjectMapper(): ObjectMapper = {
    Jackson.mapper
  }

  /** 允许跨域
    */
  override def addCorsMappings(registry: CorsRegistry): Unit = {
    registry
      .addMapping("/**")
      .allowedOrigins("*")
      .allowedMethods("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH")
      .allowCredentials(true)
      .maxAge(3600)
  }

}
