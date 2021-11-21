package io.github.dreamylost.util

import java.util.UUID

/**
  * UUID工具
  *
 * @since 2018年9月8日
  * @author 梦境迷离
  */
object UUIDUtil {

  /**
    * 64位随机UUID
    */
  def getUUID64String(): String =
    (UUID.randomUUID.toString + UUID.randomUUID.toString).replace("-", "")

  /**
    * 32位随机UUID
    */
  def getUUID32String(): String = UUID.randomUUID.toString.replace("-", "")

}
