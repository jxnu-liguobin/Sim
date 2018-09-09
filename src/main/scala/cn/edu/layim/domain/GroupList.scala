package cn.edu.layim.domain

import scala.beans.BeanProperty

/**
  * 好友分组列表
  *
  * @param id        分组id
  * @param groupName 分组名称
  */
class GroupList(id: Int, groupName: String) extends Group(id, groupName) {

    //群头像地址
    @BeanProperty
    var avatar: String = _

    //创建者Id
    @BeanProperty
    var createId: Int = _

    def this(id: Int, groupName: String, avatar: String) = {
        this(id, groupName)
        this.avatar = avatar
    }

    def this() = this(null, null)

}