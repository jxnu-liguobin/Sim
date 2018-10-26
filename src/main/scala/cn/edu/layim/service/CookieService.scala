package cn.edu.layim.service

import cn.edu.layim.entity.User
import cn.edu.layim.util.UUIDUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
  * cookie 服务
  *
  * @author 梦境迷离
  * @time 2018-10-19
  */
@Service
class CookieService @Autowired()(private val redisService: RedisService) {

    private final val LOGGER: Logger = LoggerFactory.getLogger(classOf[CookieService])

    def addCookie(user: User, request: HttpServletRequest, response: HttpServletResponse) {
        //记住用户名、密码功能(注意：cookie存放密码会存在安全隐患)
        val loginkeeping: String = request.getParameter("check")
        if ("true".equals(loginkeeping)) {
            val uID = UUIDUtil.getUUID32String()
            LOGGER.info("user uuid = " + uID)
            redisService.set(uID, JSON.toJSONString(user, SerializerFeature.DisableCircularReferenceDetect))
            val userCookie = new Cookie("uID", uID)
            userCookie.setMaxAge(30 * 24 * 60 * 60) //存活期为一个月 30*24*60*60
            userCookie.setPath("/")
            response.addCookie(userCookie)
        }
    }

    def `match`(request: HttpServletRequest): User = {

        val cookies = request.getCookies()
        //遍历所有的cookie,然后根据cookie的key值来获取value值
        var uid: String = null
        if (cookies != null) {
            for (cookie <- cookies) {
                if (cookie.getName().equals("uID")) {
                    uid = cookie.getValue()
                }
            }
        }
        if (uid == null) {
            return null
        }
        val user = redisService.get(uid).asInstanceOf[String]
        if (user != null) {
            val U = JSON.parseObject(user, classOf[User])
            if (U != null) {
                LOGGER.info("user uuid = " + uid)
                LOGGER.info("user info = " + U.toString)
                return U
            }
        }
        null
    }
}
