package cn.edu.layim.entity

import scala.beans.BeanProperty

/**
  * 消息
  *
  * @see table:t_message
  * @date 2018年9月8日
  * @author 梦境迷离
  */
class Message {

    //随便定义，用于在服务端区分消息类型
    @BeanProperty
    var Type: String = _

    //我的信息
    @BeanProperty
    var mine: Mine = _

    //对方信息
    @BeanProperty
    var to: To = _

    //额外的信息
    @BeanProperty
    var msg: String = _

    override def toString = s"Message(Type=$Type, mine=$mine, to=$to, msg=$msg)"
}
