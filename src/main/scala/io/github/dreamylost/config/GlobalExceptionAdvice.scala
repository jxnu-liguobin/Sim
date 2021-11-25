package io.github.dreamylost.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest

/** 异常处理器
  *
  * @author 梦境迷离
  * @since 2021/11/22
  * @version 1.0
  */
@ControllerAdvice
class GlobalExceptionAdvice {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[GlobalExceptionAdvice])

  @ExceptionHandler(Array(classOf[Exception]))
  def customException(request: HttpServletRequest, e: Exception): ModelAndView = {
    LOGGER.error("自定义异常处理：", e)
    val mv = new ModelAndView
    mv.addObject("message", e.getMessage)
    mv.setViewName("500")
    mv
  }
}
