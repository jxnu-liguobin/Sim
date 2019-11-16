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

  //用户id
  @BeanProperty
  var uid: Int = _

  //消息内容
  @BeanProperty
  var content: String = _

  //消息发送者id
  @BeanProperty
  var from: Int = _

  //消息发送者申请加入的群id
  @BeanProperty
  var from_group: Int = _

  //消息类型
  @BeanProperty
  var Type: Int = _

  //附言
  @BeanProperty
  var remark: String = _

  //来源，没使用，未知
  @BeanProperty
  var href: String = _

  //是否已读
  @BeanProperty
  var read: Int = _

  //时间
  @BeanProperty
  var time: String = _

  //消息发送者
  @BeanProperty
  var user: User = _

  override def toString = s"AddInfo(id=$id, uid=$uid, content=$content, from=$from, from_group=$from_group, Type=$Type, remark=$remark, href=$href, read=$read, time=$time, user=$user)"
}