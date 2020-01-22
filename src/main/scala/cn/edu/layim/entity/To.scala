package cn.edu.layim.entity

import scala.beans.BeanProperty

/**
 * 发送给...的信息
 *
 * @date 2018年9月8日
 * @author 梦境迷离
 */
class To {

  //对方的id
  @BeanProperty
  var id: Int = _

  //名字
  @BeanProperty
  var username: String = _

  //签名
  @BeanProperty
  var sign: String = _

  //头像
  @BeanProperty
  var avatar: String = _

  //状态
  @BeanProperty
  var status: String = _

  //聊天类型，一般分friend和group两种，group即群聊
  @BeanProperty
  var Type: String = _

  override def toString = s"To(id=$id, username=$username, sign=$sign, avatar=$avatar, status=$status, Type=$Type)"
}