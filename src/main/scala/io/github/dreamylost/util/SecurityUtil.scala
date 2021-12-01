package io.github.dreamylost.util

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.password.StandardPasswordEncoder

/** SpringSecurity加密工具 PasswordEncoder
  *
  * @since 2018年9月8日
  * @author 梦境迷离
  */
object SecurityUtil {

  /** 秘钥 */
  private final val SITE_WIDE_SECRET: String = "silence"

  private final lazy val encoder: PasswordEncoder = new StandardPasswordEncoder(SITE_WIDE_SECRET)

  /** 采用SHA-256算法，迭代1024次，使用一个密钥(site-wide secret)以及8位随机盐对原密码进行加密
    *
    * @param rawPassword
    * @return 80位加密后的密码
    */
  def encrypt(rawPassword: String): String = encoder.encode(rawPassword)

  /** 验证密码和加密后密码是否一致
    *
    * @param rawPassword 明文密码
    * @param password    加密后的密码
    * @return Boolean
    */
  def matched(rawPassword: String, password: String): Boolean = {
    if (rawPassword == null && password == null)
      true
    else encoder.matches(rawPassword, password)
  }

//
//  def main(args: Array[String]): Unit = {
//    val ret = encrypt("LGB123")
//    println(ret)
//  }

}
