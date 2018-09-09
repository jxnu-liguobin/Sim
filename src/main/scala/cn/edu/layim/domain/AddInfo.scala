package cn.edu.layim.domain

import cn.edu.layim.entity.User

import scala.beans.BeanProperty

/**
  * 返回添加好友、群组消息
  *
  * @date 2018年9月8日
  * @author 梦境迷离
  */
class AddInfo {

    @BeanProperty
    var id: Int = _

    @BeanProperty
    var uid: Int = _

    @BeanProperty
    var content: String = _

    @BeanProperty
    var from: Int = _

    @BeanProperty
    var from_group: Int = _

    @BeanProperty
    var Type: Int = _

    @BeanProperty
    var remark: String = _

    @BeanProperty
    var href: String = _

    @BeanProperty
    var read: Int = _

    @BeanProperty
    var time: String = _

    @BeanProperty
    var user: User = _

    override def toString = "id=" + id + ",uid=" + uid + ",content=" + content + ",from=" + from + ",from_group=" + from_group + ",Type=" + Type + ",remark=" + remark + ",href=" + href + ",read=" + read + ",time=" + time + ",user=" + user

}