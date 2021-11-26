package io.github.dreamylost.test

import io.github.dreamylost.model.domains.AddInfo

/** @author 梦境迷离
  * @version 1.0,2021/11/26
  */
object JavaCompatibleSpec extends App {

  // 无参构造使用宏生成的，IDEA暂不支持，需要后续在scala-macro-tools插件中支持这个注解

  val addInfo = new AddInfo()

  println(addInfo)

}
