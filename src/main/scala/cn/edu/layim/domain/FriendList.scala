package cn.edu.layim.domain

import java.util.List

import cn.edu.layim.entity.User

import scala.beans.BeanProperty

/**
  * 好友列表
  *
  * @param id        好友列表分组
  * @param groupName 列表名称
  */
class FriendList(id: Int, groupName: String) extends Group(id, groupName) {

    @BeanProperty
    var list: List[User] = _

    def this() = this(null)

    def this(id: Int, groupName: String, list: List[User]) = {
        this(id, groupName)
        this.list = list
    }

    def this(list: List[User]) = {
        this(null, null, list)
    }
}