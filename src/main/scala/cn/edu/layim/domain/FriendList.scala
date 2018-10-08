package cn.edu.layim.domain

import java.util.List

import cn.edu.layim.entity.{Group, User}

import scala.beans.BeanProperty

/**
  * 好友列表
  *
  * 好友列表也是一种group
  *
  * 一个好友列表有多个用户
  *
  * @param id        好友列表id
  * @param groupName 列表名称
  * @date 2018年9月8日
  * @author 梦境迷离
  */
class FriendList(id: Integer, groupName: String) extends Group(id, groupName) {

    @BeanProperty
    var list: List[User] = _

    def this(id: Integer, groupName: String, list: List[User]) = {
        this(id, groupName)
        this.list = list
    }

    def this(list: List[User]) = {
        this(null, null, list)
    }

    def this() = this(null, null)

}