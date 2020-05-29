package cn.edu.layim.config

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

/**
  * 系统拦截器配置
  *
 * @date 2018年9月8日
  * @author 梦境迷离
  */
class SystemHandlerInterceptor extends HandlerInterceptor {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[SystemHandlerInterceptor])

  /**
    * 前置处理器，在请求处理之前调用
    *
   * @param request
    * @param response
    * @param handler
    * @return Boolean
    */
  override def preHandle(
      request: HttpServletRequest,
      response: HttpServletResponse,
      handler: Object
  ): Boolean = {
    LOGGER.debug("前置处理器，在请求处理之前调用")
    if (request.getSession.getAttribute("user") == null) {
      response.sendRedirect("/")
      false
    } else true
  }

  /**
    * 请求处理之后进行调用，但是在视图被渲染之前(Controller方法调用之后)
    *
   * @param request
    * @param response
    * @param handler
    * @param modelAndView
    */
  override def postHandle(
      request: HttpServletRequest,
      response: HttpServletResponse,
      handler: Object,
      modelAndView: ModelAndView
  ): Unit = {
    LOGGER.debug("请求处理之后，视图渲染之前调用")

  }

  /**
    * 后置处理器，渲染视图完成
    *
   * @param request
    * @param response
    * @param handler
    * @param ex
    */
  override def afterCompletion(
      request: HttpServletRequest,
      response: HttpServletResponse,
      handler: Object,
      ex: Exception
  ) = {
    LOGGER.debug("后置处理器，在请求处理之后调用")

  }
}
