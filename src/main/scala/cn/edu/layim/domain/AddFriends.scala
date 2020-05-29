package cn.edu.layim.domain

import scala.beans.BeanProperty

/**
  * 添加好友
  *
 * @date 2018年9月8日
  * @author 梦境迷离
  */
class AddFriends {

  //自己的id
  @BeanProperty
  var mid: Int = _

  //分组id
  @BeanProperty
  var mgid: Int = _

  //对方用户id
  @BeanProperty
  var tid: Int = _

  //对方分组id
  @BeanProperty
  var tgid: Int = _

  def this(mid: Int, mgid: Int, tid: Int, tgid: Int) {
    this
    this.mid = mid
    this.mgid = mgid
    this.tid = tid
    this.tgid = tgid
  }

}
