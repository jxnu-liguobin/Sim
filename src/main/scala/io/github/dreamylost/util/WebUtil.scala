package io.github.dreamylost.util

import javax.servlet.http.HttpServletRequest

/** web工具
  *
  * @since 2018年9月8日
  * @author 梦境迷离
  */
object WebUtil {

  /** 获取服务器IP
    *
    * @param request
    * @return String
    */
  def getServerIpAdder(request: HttpServletRequest): String = {
    val addr: String = request.getScheme + "://" + request.getServerName
    if (request.getServerPort == 80) addr
    else addr + ":" + request.getServerPort
  }

  /** 获取客户端真实IP
    *
    * @param request
    * @return String
    */
  def getClientIpAddr(request: HttpServletRequest): String = {
    if (request == null) {
      ""
    } else {
      val ip: String = request.getHeader("x-forwarded-for")
      val ipRes = if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        request.getHeader("Proxy-Client-IP")
      } else if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        request.getHeader("WL-Proxy-Client-IP")
      } else if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        request.getRemoteAddr()
      } else if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        request.getHeader("http_client_ip")
      } else if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
        request.getHeader("HTTP_X_FORWARDED_FOR")
      }
      // 如果是多级代理，那么取第一个ip为客户ip
      else if (ip != null && ip.indexOf(",") != -1) {
        ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim()
      } else ""
      ipRes
    }
  }

}
