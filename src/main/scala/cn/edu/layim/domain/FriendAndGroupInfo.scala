package cn.edu.layim.domain

import java.util.List

import cn.edu.layim.entity.GroupList
import cn.edu.layim.entity.User

import scala.beans.BeanProperty

/**
  * 好友和群组整个信息集
  *
 * @date 2018年9月8日
  * @author 梦境迷离
  */
class FriendAndGroupInfo {

  //我的信息
  @BeanProperty
  var mine: User = _

  //好友列表
  @BeanProperty
  var friend: List[FriendList] = _

  //群组信息列表
  @BeanProperty
  var group: List[GroupList] = _

}
