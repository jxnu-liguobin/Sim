package cn.edu.layim.domain

import java.util.List

import cn.edu.layim.entity.Group
import cn.edu.layim.entity.User

import scala.beans.BeanProperty

/**
  * 好友列表
  *
 * 好友列表也是一种group
  *
 * 一个好友列表有多个用户
  *
 * @param id        好友列表id
  * @param groupname 列表名称
  * @date 2018年9月8日
  * @author 梦境迷离
  */
class FriendList(id: Integer, groupname: String) extends Group(id, groupname) {

  @BeanProperty
  var list: List[User] = _

  def this(id: Integer, groupname: String, list: List[User]) = {
    this(id, groupname)
    this.list = list
  }

  def this(list: List[User]) = {
    this(null, null, list)
  }

  def this() = this(null, null)

}
