package cn.edu.layim.websocket

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
  case class Group(groupId: Int, remark: String)

  /**
    * 同意添加好友
    */
  case class AgreeAddGroup(toUid: Int, groupId: Int, messageBoxId: Int)

}
