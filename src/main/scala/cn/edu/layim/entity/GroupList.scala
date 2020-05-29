package cn.edu.layim.entity

import scala.beans.BeanProperty

/**
  * 群组信息
  *
 * @see table:t_group
  * @param id        群组id
  * @param groupname 群组名称
  * @date 2018年9月8日
  * @author 梦境迷离
  */
class GroupList(id: Integer, groupname: String) extends Group(id, groupname) {

  //群头像地址
  @BeanProperty
  var avatar: String = _

  //创建者Id
  @BeanProperty
  var createId: Int = _

  def this(id: Integer, groupname: String, avatar: String) = {
    this(id, groupname)
    this.avatar = avatar
  }

  def this() = this(null, null)

}
