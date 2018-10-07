package cn.edu.layim.service

import java.util.Set
import java.util.concurrent.TimeUnit

import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

/**
  * redis操作
  *
  * @date 2018年9月9日
  * @author 梦境迷离
  *
  */
@Service
class RedisService {

    private val LOGGER: Logger = LoggerFactory.getLogger(classOf[RedisService])

    @Autowired
    private var redisTemplate: RedisTemplate[String, String] = _

    /**
      * 获取Set集合数据
      *
      * @param k
      * @return Set[String]
      */
    def getSets(k: String): Set[String] = {
        redisTemplate.opsForSet.members(k)
    }

    /**
      * 移除Set集合中的value
      *
      * @param k
      * @param v
      */
    def removeSetValue(k: String, v: String): Unit = {
        if (k == null && v == null) {
            return
        }
        redisTemplate.opsForSet().remove(k, v)
    }

    /**
      * 保存到Set集合中
      *
      * @param k
      * @param v
      */
    def setSet(k: String, v: String): Unit = {
        if (k == null && v == null) {
            return
        }
        redisTemplate.opsForSet().add(k, v)
    }

    /**
      * 存储Map格式
      *
      * @param key
      * @param hashKey
      * @param hashValue
      *
      */
    def setMap(key: String, hashKey: String, hashValue: String) = {
        redisTemplate.opsForHash().put(key, hashKey, hashValue)
    }

    /**
      * 存储带有过期时间的key-value
      *
      * @param key
      * @param value
      * @param timeOut 过期时间
      * @param unit    时间单位
      *
      */
    def setTime(key: String, value: String, timeOut: Long, unit: TimeUnit) = {
        if (value == null) {
            LOGGER.info("redis存储的value的值为空")
            throw new IllegalArgumentException("redis存储的value的值为空")
        }
        if (timeOut > 0) {
            redisTemplate.opsForValue().set(key, value, timeOut, unit)
        } else {
            redisTemplate.opsForValue().set(key, value)
        }
    }

    /**
      * 存储key-value
      *
      * @param key
      * @return Object
      *
      */
    def set(key: String, value: String) = {
        if (value == null) {
            LOGGER.info("redis存储的value的值为空")
            throw new IllegalArgumentException("redis存储的value的值为空")
        }
        redisTemplate.opsForValue().set(key, value)
    }

    /**
      * 根据key获取value
      *
      * @param key
      * @return Object
      *
      */
    def get(key: String): Object = redisTemplate.opsForValue().get(key)

    /**
      * 判断key是否存在
      *
      * @param key
      * @return Boolean
      *
      */
    def exists(key: String): Boolean = redisTemplate.hasKey(key)


    /**
      * 删除key对应的value
      *
      * @param key
      *
      */
    def removeValue(key: String): Unit = if (exists(key)) redisTemplate.delete(key)

    /**
      * 模式匹配批量删除key
      *
      * @param keyParttern
      *
      */
    def removePattern(keyParttern: String) = {
        val keys: Set[String] = redisTemplate.keys(keyParttern)
        if (keys.size() > 0) redisTemplate.delete(keys)
    }

}