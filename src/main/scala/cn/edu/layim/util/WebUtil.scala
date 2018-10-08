package cn.edu.layim.util

import javax.servlet.http.HttpServletRequest

/**
  * web工具
  *
  * @date 2018年9月8日
  * @author 梦境迷离
  */
object WebUtil {

    /**
      * 获取服务器IP
      *
      * @param request
      * @return String
      */
    def getServerIpAdder(request: HttpServletRequest): String = {
        val addr: String = request.getScheme + "://" + request.getServerName
        if (request.getServerPort == 80) {
            return addr
        } else {
            addr + ":" + request.getServerPort
        }
    }

    /**
      * 获取客户端真实IP
      *
      * @param request
      * @return String
      */
    def getClientIpAddr(request: HttpServletRequest): String = {
        if (request == null) {
            return ""
        }
        var ip: String = request.getHeader("x-forwarded-for")

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr()
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("http_client_ip")
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        // 如果是多级代理，那么取第一个ip为客户ip
        if (ip != null && ip.indexOf(",") != -1) {
            ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length()).trim()
        }
        ip
    }

}