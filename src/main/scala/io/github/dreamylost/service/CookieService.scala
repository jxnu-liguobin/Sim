package io.github.dreamylost.service

import io.github.dreamylost.model.entities.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

import java.util.Base64
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/** cookie 服务
  *
  * @author 梦境迷离
  * @since 2018-10-19
  */
@Service
class CookieService {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[CookieService])

  def addCookie(user: User, request: HttpServletRequest, response: HttpServletResponse) {
    val baseE: Base64.Encoder = Base64.getEncoder
    val baseD: Base64.Decoder = Base64.getDecoder
    //记住用户名、密码功能(注意：cookie存放密码会存在安全隐患)
    val loginkeeping: String = request.getParameter("check")
    if ("true" == loginkeeping) {
      //使用token，通过Redis
      //val uID = UUIDUtil.getUUID32String()
      LOGGER.info(s"add cookie for user => [email = ${user.email}]")
      //简单处理，cookie key不能使用=号
      val userCookie = new Cookie(
        new String(baseE.encode(user.email.getBytes)).replace("=", ""),
        new String(baseE.encode(user.password.getBytes)).replace("=", "")
      )
      userCookie.setMaxAge(30 * 24 * 60 * 60) //存活期为一个月 30*24*60*60
      userCookie.setPath("/")
      response.addCookie(userCookie)
    } else {
      try {
        //没有勾选时，清楚cookie
        val cookies = request.getCookies
        for (cookie <- cookies) {
          val cookieName = new String(baseD.decode(cookie.getName))
          if (cookieName == user.email) {
            LOGGER.info(
              s"remove cookie for user => [email = ${user.email}, cookie name = $cookieName]"
            )
            cookie.setMaxAge(0)
            cookie.setPath("/")
            response.addCookie(cookie)
          }
        }
      } catch {
        case e: Exception =>
          LOGGER.error(s"failed in cookie service: $e")
      }
    }
  }
}
