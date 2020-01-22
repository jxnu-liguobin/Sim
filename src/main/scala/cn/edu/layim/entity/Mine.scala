package cn.edu.layim.entity

import scala.beans.BeanProperty

/**
 * 我发送的消息和我的信息
 *
 * @date 2018年9月8日
 * @author 梦境迷离
 */
class Mine {

  //我的id
  @BeanProperty
  var id: Int = _

  //我的昵称
  @BeanProperty
  var username: String = _

  //是否我发的消息
  @BeanProperty
  var mine: Boolean = _

  //我的头像
  @BeanProperty
  var avatar: String = _

  //消息内容
  @BeanProperty
  var content: String = _

  override def toString = s"Mine(id=$id, username=$username, mine=$mine, avatar=$avatar, content=$content)"
}