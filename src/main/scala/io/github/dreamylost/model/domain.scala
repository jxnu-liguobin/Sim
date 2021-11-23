package io.github.dreamylost.model

import entity.GroupList
import entity.User

/**
  *
 * @author 梦境迷离
  * @since 2021/11/21
  * @version 1.0
  */
object domain {

  import scala.beans.BeanProperty

  /**
    * 我发送的消息和我的信息
    *
   * @param id       我的id
    * @param username 我的昵称
    * @param mine     是否我发的消息
    * @param avatar   我的头像
    * @param content  消息内容
    */
  case class Mine(id: Int, username: String, mine: Boolean, avatar: String, content: String)

  /**
    * 发送给...的信息
    *
   * @param id       对方的id
    * @param username 名字
    * @param sign     签名
    * @param avatar   头像
    * @param status   状态
    * @param `type`     聊天类型，一般分friend和group两种，group即群聊
    */
  case class To(
      id: Int,
      username: String,
      sign: String,
      avatar: String,
      status: String,
      `type`: String
  )

  /**
    * 聊天记录
    *
   * @param id        用户id
    * @param username  用户名
    * @param avatar    用户头像
    * @param content   消息内容
    * @param timestamp 时间
    */
  case class ChatHistory(
      @BeanProperty id: Int,
      @BeanProperty username: String,
      @BeanProperty avatar: String,
      @BeanProperty content: String,
      @BeanProperty timestamp: Long
  )

  /**
    * 好友和群组整个信息集
    *
   * @param mine   我的信息
    * @param friend 好友列表
    * @param group  群组信息列表
    */
  case class FriendAndGroupInfo(@BeanProperty mine: User,
                                @BeanProperty friend: List[FriendList],
                                @BeanProperty group: List[GroupList])

  /**
    * 群组
    *
   * @param id        群组id
    * @param groupname 群组名
    */
  class Group(@BeanProperty val id: Int, @BeanProperty val groupname: String)

  /**
    * 好友列表
    *
   * 好友列表也是一种group
    *
   * 一个好友列表有多个用户
    *
   * @param id        好友列表id
    * @param groupname 列表名称
    * @param list      用户列表
    */
  case class FriendList(
      @BeanProperty override val id: Int,
      @BeanProperty override val groupname: String,
      @BeanProperty list: List[User]
  ) extends Group(id, groupname) {
    def this() = {
      this(0, null, null)
    }
  }

  /**
    * 返回个人信息更新
    *
   * @param id
    * @param username 用户名
    * @param password 密码
    * @param oldpwd   旧密码
    * @param sign     签名
    * @param sex      性别
    */
  case class UserVo(
      id: Int,
      username: String,
      password: String,
      oldpwd: String,
      sign: String,
      sex: String
  )

  /**
    * 添加好友、群组
    *
   * @param groupId 好友列表id或群组id
    * @param remark  附言
    * @param `type`    类型，好友或群组
    */
  case class Add(groupId: Int, remark: String, `type`: Int)

  /**
    * 添加好友
    *
   * @param mid  自己的id
    * @param mgid 分组id
    * @param tid  对方用户id
    * @param tgid 对方分组id
    */
  case class AddFriends(mid: Int, mgid: Int, tid: Int, tgid: Int)

  /**
    * 返回添加好友、群组消息
    *
   * @param id
    * @param uid        用户id
    * @param content    消息内容
    * @param from       消息发送者id
    * @param from_group 消息发送者申请加入的群id
    * @param `type`       消息类型
    * @param remark     附言
    * @param href       来源，没使用，未知
    * @param read       是否已读
    * @param time       时间
    * @param user       消息发送者
    */
  case class AddInfo(
      @BeanProperty id: Int,
      @BeanProperty uid: Int,
      @BeanProperty content: String,
      @BeanProperty from: Int,
      @BeanProperty from_group: Int,
      @BeanProperty `type`: Int,
      @BeanProperty remark: String,
      @BeanProperty href: String,
      @BeanProperty read: Int,
      @BeanProperty time: String,
      @BeanProperty user: User
  )

  /**
    * 收到的消息
    *
   * @param toid      发送给哪个用户
    * @param id        消息的来源ID（如果是私聊，则是用户id，如果是群聊，则是群组id）
    * @param username  消息来源用户名
    * @param avatar    消息来源用户头像
    * @param `type`      聊天窗口来源类型，从发送消息传递的to里面获取
    * @param content   消息内容
    * @param cid       消息id，可不传。除非你要对消息进行一些操作（如撤回）
    * @param mine      是否我发送的消息，如果为true，则会显示在右方
    * @param fromid    消息的发送者id（比如群组中的某个消息发送者），可用于自动解决浏览器多窗口时的一些问题
    * @param timestamp 服务端动态时间戳
    * @param status    消息的状态
    */
  case class Receive(
      toid: Int,
      id: Int,
      username: String,
      avatar: String,
      `type`: String,
      content: String,
      cid: Int,
      mine: Boolean,
      fromid: Int,
      timestamp: Long,
      status: Int
  )

}
