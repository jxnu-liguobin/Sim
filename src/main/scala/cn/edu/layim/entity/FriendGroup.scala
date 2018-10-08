package cn.edu.layim.entity

import scala.beans.BeanProperty

/**
  * 用户创建的好友列表
  *
  * @see table:t_friend_group
  * @date 2018年9月8日
  * @author 梦境迷离
  */
class FriendGroup {

    //用户id，该分组所属的用户ID
    @BeanProperty
    var uid: Int = _

    //群组名称
    @BeanProperty
    var groupName: String = _

    def this(uid: Int, groupName: String) = {
        this
        this.uid = uid
        this.groupName = groupName
    }

    override def toString = "uid = " + uid + ",groupName = " + groupName

}