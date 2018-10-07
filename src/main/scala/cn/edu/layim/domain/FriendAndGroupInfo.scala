package cn.edu.layim.domain

import java.util.List

import cn.edu.layim.entity.User

import scala.beans.BeanProperty

/**
  * 好友和群组信息
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

    //群组分组
    @BeanProperty
    var group: List[GroupList] = _

}