package cn.edu.layim.domain

import scala.beans.BeanProperty

/**
  * 添加好友、群组
  *
 * @date 2018年9月8日
  * @author 梦境迷离
  */
class Add {

  //好友列表id或群组id
  @BeanProperty
  var groupId: Int = _

  //附言
  @BeanProperty
  var remark: String = _

  //类型，好友或群组
  @BeanProperty
  var Type: Int = _

  override def toString = s"Add(groupId=$groupId, remark=$remark, Type=$Type)"
}
