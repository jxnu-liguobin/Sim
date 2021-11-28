package io.github.dreamylost.config

import io.github.dreamylost.log
import io.github.dreamylost.logs.LogType
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/** 系统拦截器配置
  *
  * @since 2018年9月8日
  * @author 梦境迷离
  */
@log(logType = LogType.Slf4j)
class SystemHandlerInterceptor extends HandlerInterceptor {

  /** 前置处理器，在请求处理之前调用
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
    log.debug("前置处理器，在请求处理之前调用")
    if (request.getSession.getAttribute("user") == null) {
      response.sendRedirect("/")
      false
    } else true
  }

  /** 请求处理之后进行调用，但是在视图被渲染之前(Controller方法调用之后)
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
    log.debug("请求处理之后，视图渲染之前调用")
  }

  /** 后置处理器，渲染视图完成
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
  ): Unit = {
    log.debug("后置处理器，在请求处理之后调用")
  }
}
