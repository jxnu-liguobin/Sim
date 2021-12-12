package io.github.dreamylost.model

import org.bitlap.tools.JavaCompatible

import java.util.Date

/** @author 梦境迷离
  * @since 2021/11/21
  * @version 1.0
  */
object entities {

  /** 用户
    *
    * @see table:t_user
    * @param id
    * @param username   用户名
    * @param password   密码
    * @param sign       签名
    * @param avatar     头像
    * @param email      邮箱
    * @param createDate 创建时间
    * @param sex        性别
    * @param status     状态
    * @param active     激活码
    */
  @JavaCompatible case class User(
      id: Int,
      username: String,
      password: String,
      sign: String,
      avatar: String,
      email: String,
      createDate: Date,
      sex: Int,
      status: String,
      active: String
  )

  object User {
    def apply(id: Int, status: String): User =
      User(
        id = id,
        username = null,
        password = null,
        sign = null,
        avatar = null,
        email = null,
        createDate = null,
        sex = 0,
        status = status,
        active = null
      )
  }

  /** 添加消息
    *
    * @see table:t_add_message
    * @param id
    * @param fromUid 谁发起的请求
    * @param toUid   发送给谁的申请,可能是群，那么就是创建该群组的用户
    * @param groupId 如果是添加好友则为from_id的分组id，如果为群组则为群组id
    * @param remark  附言
    * @param agree   0未处理，1同意，2拒绝
    * @param `type`  类型，可能是添加好友或群组
    * @param time    申请时间
    */
  @JavaCompatible case class AddMessage(
      id: Int,
      fromUid: Int,
      toUid: Int,
      groupId: Int,
      remark: String,
      agree: Int,
      `type`: Int,
      time: Date
  )

  object AddMessage {
    def apply(id: Int, agree: Int): AddMessage =
      AddMessage(
        id = id,
        fromUid = 0,
        toUid = 0,
        groupId = 0,
        remark = null,
        agree = agree,
        `type` = 0,
        time = null
      )

    def apply(
        fromUid: Int,
        toUid: Int,
        groupId: Int,
        remark: String,
        `type`: Int,
        time: Date
    ): AddMessage =
      AddMessage(0, fromUid, toUid, groupId, remark, 0, `type`, time)
  }

  /** 用户创建的好友列表
    *
    * @see table:t_friend_group
    * @param id        分组ID
    * @param uid       用户id，该分组所属的用户ID
    * @param groupname 群组名称
    */
  @JavaCompatible case class FriendGroup(id: Int, uid: Int, groupname: String)

  /** 群组信息
    *
    * @see table:t_group
    * @param id        群组id
    * @param groupname 群组名称
    * @param avatar    头像
    * @param createId  创建人ID
    */
  @JavaCompatible case class GroupList(
      override val id: Int,
      override val groupname: String,
      avatar: String,
      createId: Int
  ) extends Group(id, groupname)

  /** 群组成员
    *
    * @see table:t_group_members
    * @param gid 群组编号
    * @param uid 用户编号
    */
  @JavaCompatible case class GroupMember(gid: Int, uid: Int)

  /** 消息
    *
    * @see table:t_message
    * @param `type` 随便定义，用于在服务端区分消息类型
    * @param mine   我的信息
    * @param to     对方信息
    * @param msg    额外的信息
    */
  @JavaCompatible case class Message(`type`: String, mine: Mine, to: To, msg: String)

}
