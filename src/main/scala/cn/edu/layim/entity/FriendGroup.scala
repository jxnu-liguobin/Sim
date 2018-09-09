package cn.edu.layim.entity

import scala.beans.BeanProperty

/**
  * 好友分组对象
  *
  * @date 2018年9月8日
  * @author 梦境迷离
  */
class FriendGroup {

    //用户id
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