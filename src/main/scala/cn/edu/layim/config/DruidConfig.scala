package cn.edu.layim.config

import com.alibaba.druid.support.http.{ StatViewServlet, WebStatFilter }
import org.springframework.boot.web.servlet.{ FilterRegistrationBean, ServletRegistrationBean }
import org.springframework.context.annotation.{ Bean, Configuration }

import scala.collection.JavaConverters._

/**
 * Alibaba Druid数据源配置
 *
 * @date 2018年9月8日
 * @author 梦境迷离
 */
@Configuration
class DruidConfig {

  /**
   * druid配置访问路径和用户名密码
   *
   * @return ServletRegistrationBean
   */
  @Bean
  def statViewServlet(): ServletRegistrationBean = {
    val druid = new ServletRegistrationBean()
    druid.setServlet(new StatViewServlet())
    druid.setUrlMappings(List("/druid/*").asJava)
    val params = Map("loginUsername" -> "admin", "loginPassword" -> "admin", "allo" -> "", "resetEnable" -> "false")
    druid.setInitParameters(params.asJava)
    druid
  }

  /**
   * 拦截器配置
   *
   * @return FilterRegistrationBean
   */
  @Bean
  def webStatFilter(): FilterRegistrationBean = {
    val filter = new FilterRegistrationBean()
    filter.setFilter(new WebStatFilter())
    filter.setUrlPatterns(List("/*").asJava)
    filter.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*")
    filter
  }

}