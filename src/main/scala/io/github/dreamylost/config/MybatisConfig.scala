package io.github.dreamylost.config

import com.github.pagehelper.PageHelper
import org.bitlap.tools.log
import org.bitlap.tools.logs.LogType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.Properties

/** Mybatis的分页插件
  *
  * @since 2018年9月8日
  * @author 梦境迷离
  */
@Configuration
@log(logType = LogType.Slf4j)
class MybatisConfig {

  @Bean
  def pageHelper(): PageHelper = {
    log.info("注册MyBatis分页插件PageHelper")
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
