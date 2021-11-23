package io.github.dreamylost.config

import org.slf4j.{ Logger, LoggerFactory }
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.{ CachingConfigurerSupport, EnableCaching }
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.{ Bean, Configuration }
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.{ RedisTemplate, StringRedisTemplate }
import org.springframework.data.redis.serializer.{ JdkSerializationRedisSerializer, StringRedisSerializer }

import java.lang.reflect.Method

/**
 * redis缓存管理配置
 *
 * @since 2018年9月8日
 * @author 梦境迷离
 */
@EnableCaching
@Configuration
class CacheConfig extends CachingConfigurerSupport {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[CacheConfig])

  //允许超时
  @Value("${spring.redis.timeout}")
  private var timeout: Int = _

  @Bean
  def cacheManager(redisTemplate: RedisTemplate[String, String]): CacheManager = {
    val cacheManager = new RedisCacheManager(redisTemplate)
    //设置key-value过期时间
    cacheManager.setDefaultExpiration(timeout)
    LOGGER.info("初始化Redis缓存管理器完成!")
    cacheManager
  }

  /**
   * 缓存保存策略
   *
   * @return KeyGenerator
   */
  @Bean
  def wiselyKeyGenerator(): KeyGenerator = {
    new KeyGenerator() {
      override def generate(target: Any, method: Method, params: AnyRef*) = {
        val sb = new StringBuilder
        sb.append(target.getClass.getName)
        sb.append(method.getName)
        for (param <- params) {
          sb.append(param.toString)
        }
        sb.toString
      }
    }
  }

  @Bean
  def redisTemplate(factory: RedisConnectionFactory): RedisTemplate[String, String] = {
    val template = new StringRedisTemplate(factory)
    setSerializer(template)
    template.afterPropertiesSet()
    template
  }

  private def setSerializer(template: StringRedisTemplate): Unit = {
    val jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer
    template.setKeySerializer(new StringRedisSerializer())
    template.setDefaultSerializer(jdkSerializationRedisSerializer)
    template.setValueSerializer(jdkSerializationRedisSerializer)
    template.setHashKeySerializer(jdkSerializationRedisSerializer)
    template.setHashValueSerializer(jdkSerializationRedisSerializer)
  }
}
