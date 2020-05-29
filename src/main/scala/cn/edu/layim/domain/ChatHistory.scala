package cn.edu.layim.domain

import scala.beans.BeanProperty

/**
  * 聊天记录
  *
 * @data 2018年9月8日
  * @author 梦境迷离
  */
class ChatHistory {

  //用户id
  @BeanProperty
  var id: Int = _

  //用户名
  @BeanProperty
  var username: String = _

  //用户头像
  @BeanProperty
  var avatar: String = _

  //消息内容
  @BeanProperty
  var content: String = _

  //时间
  @BeanProperty
  var timestamp: Long = _

  def this(id: Int, username: String, avatar: String, content: String, timestamp: Long) = {
    this
    this.id = id
    this.username = username
    this.avatar = avatar
    this.content = content
    this.timestamp = timestamp
  }

}
