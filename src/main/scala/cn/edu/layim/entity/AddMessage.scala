package cn.edu.layim.entity

import java.util.Date

import scala.beans.BeanProperty

/**
  * 添加消息
  *
  * @see table:t_add_message
  * @date 2018年9月8日
  * @author 梦境迷离
  */
class AddMessage {

    @BeanProperty
    var id: Int = _

    //谁发起的请求
    @BeanProperty
    var fromUid: Int = _

    //发送给谁的申请,可能是群，那么就是创建该群组的用户
    @BeanProperty
    var toUid: Int = _

    //如果是添加好友则为from_id的分组id，如果为群组则为群组id
    @BeanProperty
    var groupId: Int = _

    //附言
    @BeanProperty
    var remark: String = _

    //0未处理，1同意，2拒绝
    @BeanProperty
    var agree: Int = _

    //类型，可能是添加好友或群组
    @BeanProperty
    var Type: Int = _

    //申请时间
    @BeanProperty
    var time: Date = _

    override def toString = s"AddMessage(id=$id, fromUid=$fromUid, toUid=$toUid, groupId=$groupId, remark=$remark, agree=$agree, Type=$Type, time=$time)"
}