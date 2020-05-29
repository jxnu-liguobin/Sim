package cn.edu.layim.config

import java.util.Properties

import com.github.pagehelper.PageHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
  * Mybatis的分页插件
  *
 * @date 2018年9月8日
  * @author 梦境迷离
  */
@Configuration
class MybatisConfig {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[MybatisConfig])

  @Bean
  def pageHelper(): PageHelper = {
    LOGGER.info("注册MyBatis分页插件PageHelper")
    val pageHelper = new PageHelper()
    val properties = new Properties()
    properties.setProperty(
      "offsetAsPageNum",
      "true"
    ) //将RowBounds第一个参数offset当成pageNum页码使用和startPage中的pageNum效果一样
    properties.setProperty("rowBoundsWithCount", "true") //使用RowBounds分页会进行count查询
    properties.setProperty("reasonable", "true") //合理化
    pageHelper.setProperties(properties)
    pageHelper
  }

}
