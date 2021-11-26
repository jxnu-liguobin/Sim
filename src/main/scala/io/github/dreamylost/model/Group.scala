package io.github.dreamylost.model

import scala.beans.BeanProperty

/** 群组
  *
  * @param id        群组id
  * @param groupname 群组名
  */
@SerialVersionUID(1L) class Group(
    @BeanProperty val id: Int,
    @BeanProperty val groupname: String
)
