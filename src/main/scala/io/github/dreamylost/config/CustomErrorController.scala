package io.github.dreamylost.config

import org.bitlap.tools.log
import org.bitlap.tools.logs.LogType
import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

/** 错误处理器
  *
  * @author 梦境迷离
  * @since 2021/11/28
  * @version 1.0
  */
@Controller
@log(logType = LogType.Slf4j)
class CustomErrorController extends ErrorController {

  private final val ERROR_PATH = "/error"

  @RequestMapping(value = Array(ERROR_PATH))
  def handleError: String = "404"

  def getErrorPath: String = ERROR_PATH
}
