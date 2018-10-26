package cn.edu.layim.websocket.domain

import scala.beans.BeanProperty

/**
  * 服务器端WebSocket领域对象
  *
  * @date 2018年9月8日
  * @author 梦境迷离
  */
object Domain {

    /**
      * 添加群信息
      */
    class Group {

        @BeanProperty
        var groupId: Int = _

        @BeanProperty
        var remark: String = _

        override def toString = s"Group(groupId=$groupId, remark=$remark)"
    }

    /**
      * 同意添加好友
      */
    class AgreeAddGroup {

        @BeanProperty
        var toUid: Int = _

        @BeanProperty
        var groupId: Int = _

        @BeanProperty
        var messageBoxId: Int = _

        override def toString = s"AgreeAddGroup(toUid=$toUid, groupId=$groupId, messageBoxId=$messageBoxId)"
    }

}