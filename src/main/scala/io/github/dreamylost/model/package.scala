package io.github.dreamylost

import scala.beans.BeanProperty

/** @author 梦境迷离
  * @since 2021/11/24
  * @version 1.0
  */
package object model {

  /** 群组
    *
    * @param id        群组id
    * @param groupname 群组名
    */
  @SerialVersionUID(1L) class Group(
      @BeanProperty val id: Int,
      @BeanProperty val groupname: String
  ) {
    def this() = {
      this(0, null)
    }
  }

}
