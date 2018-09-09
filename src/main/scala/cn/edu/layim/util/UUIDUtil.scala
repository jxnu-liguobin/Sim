package cn.edu.layim.util

import java.util.UUID

/**
  * UUID工具
  *
  * @dete 2018年9月8日
  * @author 梦境迷离
  */
object UUIDUtil {

    /**
      * 64位随机UUID
      *
      * @return String
      */
    def getUUID64String(): String = (UUID.randomUUID.toString + UUID.randomUUID.toString).replace("-", "")

    /**
      * 32位随机UUID
      *
      * @return String
      */
    def getUUID32String(): String = UUID.randomUUID.toString.replace("-", "")

}