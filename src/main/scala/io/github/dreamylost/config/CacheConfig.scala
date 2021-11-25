package io.github.dreamylost.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachingConfigurerSupport
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.ScalaObjectMapper

import java.lang.reflect.Method

/** redis缓存管理配置
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
    LOGGER.info("Init the CacheManager Finished")
    cacheManager
  }

  /** 缓存保存策略
    *
    * @return KeyGenerator
    */
  @Bean
  def wiselyKeyGenerator(): KeyGenerator = {
    new KeyGenerator() {
      override def generate(target: Any, method: Method, params: AnyRef*): String = {
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
    val objectMapper = new ObjectMapper() with ScalaObjectMapper
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
    objectMapper.registerModule(DefaultScalaModule)
    objectMapper.activateDefaultTyping(
      LaissezFaireSubTypeValidator.instance,
      ObjectMapper.DefaultTyping.EVERYTHING
    )

    // 必须使用这个序列化Scala+java.util.List
    val genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper)
    template.setKeySerializer(new StringRedisSerializer())
    template.setDefaultSerializer(genericJackson2JsonRedisSerializer)
    template.setValueSerializer(genericJackson2JsonRedisSerializer)
    template.setHashKeySerializer(genericJackson2JsonRedisSerializer)
    template.setHashValueSerializer(genericJackson2JsonRedisSerializer)
  }
}
