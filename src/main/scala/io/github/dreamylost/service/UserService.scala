package io.github.dreamylost.service

import io.github.dreamylost.constant.SystemConstant
import org.bitlap.tools.log
import org.bitlap.tools.logs.LogType
import io.github.dreamylost.model.domains._
import io.github.dreamylost.model.entities._
import io.github.dreamylost.repository.UserRepository
import io.github.dreamylost.util.DateUtil
import io.github.dreamylost.util.SecurityUtil
import io.github.dreamylost.util.UUIDUtil
import io.github.dreamylost.util.WebUtil
import io.github.dreamylost.websocket.WebSocketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util
import java.util.logging.{ Logger => _ }
import javax.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters._

/** 用户信息相关操作
  *
  * @since 2018年9月9日
  * @author 梦境迷离
  */
@Service
@log(logType = LogType.Slf4j)
class UserService @Autowired() (userRepository: UserRepository, mailService: MailService) {

  @Autowired
  private var wsService: WebSocketService = _

  /** 退出群
    *
    * @param gid 群组id
    * @param uid 用户
    * @return Boolean
    */
  @CacheEvict(
    value = Array("findUserById", "findGroupsById", "findUserByGroupId", "findGroupById"),
    allEntries = true
  )
  @Transactional
  def leaveOutGroup(gid: Int, uid: Int): Boolean = {
    //创建者退群，直接解散群,此处逻辑可自行调整
    val group = userRepository.findGroupById(gid)
    if (group == null) return false
    if (group.createId.equals(uid)) {
      // 群主退出
      val users = userRepository.findGroupMembers(gid)
      val master = findUserById(group.createId)
      users.asScala.foreach { uid =>
        wsService.deleteGroup(master, group.groupname, gid, uid)
        userRepository.leaveOutGroup(GroupMember(gid, uid))
      }
      userRepository.deleteGroup(gid) == 1
    } else {
      userRepository.leaveOutGroup(GroupMember(gid, uid)) == 1
    }
  }

  /** 根据ID查找群
    *
    * @param gid
    * @return
    */
  @Cacheable(value = Array("findGroupById"), keyGenerator = "wiselyKeyGenerator")
  def findGroupById(gid: Int): GroupList = {
    userRepository.findGroupById(gid)
  }

  /** 添加群成员
    *
    * @param gid          群组id
    * @param uid          用户id
    * @param messageBoxId 消息盒子Id
    * @return Boolean
    */
  @Transactional
  @CacheEvict(value = Array("findUserByGroupId", "findGroupsById"), allEntries = true)
  def addGroupMember(gid: Int, uid: Int, messageBoxId: Int): Boolean = {
    val group = userRepository.findGroupById(gid)
    if (group == null) return false
    if (group != null && group.createId.equals(uid)) {
      //自己加自己的群，默认同意
      updateAddMessage(messageBoxId, 1)
      true
    } else {
      userRepository.addGroupMember(GroupMember(gid, uid)) == 1
      updateAddMessage(messageBoxId, 1)
    }
  }

  /** 用户创建群时，将自己加入群组，不需要提示
    *
    * @param gid 群组id
    * @param uid 用户id
    * @return Boolean
    */
  @CacheEvict(value = Array("findGroupsById", "findUserByGroupId"), allEntries = true)
  @Transactional
  def addGroupMember(gid: Int, uid: Int): Boolean = {
    userRepository.addGroupMember(new GroupMember(gid, uid)) == 1
  }

  /** 删除好友
    *
    * @param friendId 好友id
    * @param uId      个人/用户id
    * @return Boolean
    */
  @CacheEvict(
    value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"),
    allEntries = true
  )
  @Transactional
  def removeFriend(friendId: Int, uId: Int): Boolean = {
    userRepository.removeFriend(friendId, uId) == 1
  }

  /** 更新用户头像
    *
    * @param userId 个人id
    * @param avatar 头像
    * @return Boolean
    */
  @CacheEvict(value = Array("findUserById"), allEntries = true)
  @Transactional
  def updateAvatar(userId: Int, avatar: String): Boolean = {
    userRepository.updateAvatar(userId, avatar) == 1
  }

  /** 更新用户信息
    *
    * @param user 个人信息
    * @return Boolean
    */
  @CacheEvict(
    value = Array("findUserById", "findUserByGroupId", "findFriendGroupsById"),
    allEntries = true
  )
  @Transactional
  def updateUserInfo(user: User): Boolean = {
    userRepository.updateUserInfo(user) == 1
  }

  /** 更新用户状态
    *
    * @param user 个人信息
    * @return Boolean
    */
  @CacheEvict(
    value = Array("findUserById", "findUserByGroupId", "findFriendGroupsById"),
    allEntries = true
  )
  @Transactional
  def updateUserStatus(user: User): Boolean = {
    userRepository.updateUserStatus(user) == 1
  }

  /** 移动好友分组
    *
    * @param groupId 新的分组id
    * @param uId     被移动的好友id
    * @param mId     我的id
    * @return Boolean
    */
  //清除缓存
  @CacheEvict(
    value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"),
    allEntries = true
  )
  @Transactional
  def changeGroup(groupId: Int, uId: Int, mId: Int): Boolean = {
    val originRecordId = userRepository.findUserGroup(uId, mId)
    if (originRecordId != null) {
      userRepository.changeGroup(groupId, originRecordId) == 1
    } else false
  }

  /** 添加好友操作
    *
    * @param mid          我的id
    * @param mgid         我设定的分组
    * @param tid          对方的id
    * @param tgid         对方设定的分组
    * @param messageBoxId 消息盒子的消息id
    * @return Boolean
    */
  @Transactional
  @CacheEvict(
    value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"),
    allEntries = true
  )
  def addFriend(mid: Int, mgid: Int, tid: Int, tgid: Int, messageBoxId: Int): Boolean = {
    val add = AddFriends(mid, mgid, tid, tgid)
    try {
      if (userRepository.addFriend(add) != 0) updateAddMessage(messageBoxId, 1)
      else false
    } catch {
      case ex: Exception =>
        log.error("重复添好友", ex)
        false
    }
  }

  /** 创建好友分组列表
    *
    * @param uid       个人id
    * @param groupname 群组id
    * @return Boolean FriendGroup
    */
  @CacheEvict(value = Array("findFriendGroupsById"), allEntries = true)
  @Transactional
  def createFriendGroup(groupname: String, uid: Int): Int = {
    userRepository.createFriendGroup(FriendGroup(0, uid, groupname))
  }

  /** 创建群组
    *
    * @param groupList 群
    * @return Boolean
    */
  @CacheEvict(value = Array("findGroupsById"), allEntries = true)
  @Transactional
  def createGroup(groupList: GroupList): Int = {
    if (groupList == null) -1
    else {
      userRepository.createGroupList(groupList)
      val id = groupList.id
      if (id > 0) id else -1
    }
  }

  /** 统计未处理消息
    *
    * @param uid   个人id
    * @param agree 0未处理，1同意，2拒绝
    * @return Int
    */
  def countUnHandMessage(uid: Int, agree: Integer): Int =
    userRepository.countUnHandMessage(uid, agree)

  /** 查询添加好友、群组信息
    *
    * @param uid 个人id
    * @return List[AddInfo]
    */
  def findAddInfo(uid: Int): util.List[AddInfo] = {
    val list = userRepository.findAddInfo(uid)
    val ret = new util.ArrayList[AddInfo](list.size())
    list.asScala.foreach { info =>
      val infoCopy = if (info.`type` == 0) {
        info.copy(content = "申请添加你为好友")
      } else {
        val group: GroupList = userRepository.findGroupById(info.from_group)
        if (group != null) {
          info.copy(content = "申请加入 '" + group.groupname + "' 群聊中!")
        } else info
      }
      log.info(infoCopy.toString)
      ret.add(infoCopy.copy(href = null, user = findUserById(infoCopy.from)))
    }
    ret
  }

  /** 更新好友、群组信息请求
    *
    * @param messageBoxId 消息盒子id
    * @param agree        0未处理，1同意，2拒绝
    * @return Boolean
    */
  @Transactional
  def updateAddMessage(messageBoxId: Int, agree: Int): Boolean = {
    userRepository.updateAddMessage(AddMessage(agree = agree, id = messageBoxId)) == 1
  }

  @Transactional
  def refuseAddFriend(messageBoxId: Int, user: User, to: Int): Boolean = {
    wsService.refuseAddFriend(messageBoxId, user, to)
  }

  /** 好友消息已读
    *
    * @param mine
    * @param to
    * @return
    */
  @Transactional
  def readFriendMessage(mine: Int, to: Int): Boolean = {
    userRepository.readMessage(mine, to, SystemConstant.FRIEND_TYPE) == 1
  }

  /** 将本群中的所有消息对我标记为已读
    *
    * @param gId
    * @param to 群离线消息的接收人to就是群的ID
    * @return
    */
  @Transactional
  def readGroupMessage(gId: Int, to: Int): Boolean = {
    userRepository.readMessage(gId, to, SystemConstant.GROUP_TYPE) == 1
  }

  /** 添加好友、群组信息请求
    *
    * @param addMessage 添加好友、群组信息对象
    * @see AddMessage.scala
    * @return Int
    */
  @Transactional
  def saveAddMessage(addMessage: AddMessage): Int = {
    userRepository.saveAddMessage(addMessage)
  }

  /** 根据群名模糊统计
    *
    * @param groupName 群组名称
    * @return Int
    */
  def countGroup(groupName: String): Int = userRepository.countGroup(groupName)

  /** 根据群名模糊查询群
    *
    * @param groupName 群组名称
    * @return List[GroupList]
    */
  def findGroup(groupName: String): util.List[GroupList] = userRepository.findGroup(groupName)

  /** 根据用户名和性别统计用户
    *
    * @param username 用户名
    * @param sex      性别
    * @return Int
    */
  def countUsers(username: String, sex: Integer): Int = userRepository.countUser(username, sex)

  /** 根据用户名和性别查询用户
    *
    * @param username 用户名
    * @param sex      性别
    * @return List[User]
    */
  def findUsers(username: String, sex: Integer): util.List[User] =
    userRepository.findUsers(username, sex)

  /** 统计查询消息
    *
    * @param uid    消息所属用户id、用户个人id
    * @param mid    来自哪个用户
    * @param `type` 消息类型，可能来自friend或者group
    * @return Int
    */
  def countHistoryMessage(uid: Int, mid: Int, `type`: String): Int = {
    `type` match {
      case SystemConstant.FRIEND_TYPE => userRepository.countHistoryMessage(uid, mid, `type`)
      case SystemConstant.GROUP_TYPE => userRepository.countHistoryMessage(null, mid, `type`)
    }
  }

  /** 查询历史消息
    *
    * @param user   所属用户、用户个人
    * @param mid    来自哪个用户
    * @param `type` 消息类型，可能来自friend或者group
    * @see User.scala
    * @return List[ChatHistory]
    */
  def findHistoryMessage(user: User, mid: Int, `type`: String): util.List[ChatHistory] = {
    //单人聊天记录
    val list = if (SystemConstant.FRIEND_TYPE.equals(`type`)) {
      //查找聊天记录
      val historys: util.List[Receive] = userRepository.findHistoryMessage(user.id, mid, `type`)
      val toUser = findUserById(mid)
      historys.asScala.map { history =>
        if (history.id == mid) {
          ChatHistory(
            history.id,
            toUser.username,
            toUser.avatar,
            history.content,
            history.timestamp
          )
        } else {
          ChatHistory(history.id, user.username, user.avatar, history.content, history.timestamp)
        }
      }
    } else if (SystemConstant.GROUP_TYPE.equals(`type`)) {
      //群聊天记录
      //查找聊天记录
      val historys = userRepository.findHistoryMessage(null, mid, `type`)
      historys.asScala.map { history =>
        val u = findUserById(history.fromid)
        if (history.fromid.equals(user.id)) {
          ChatHistory(user.id, user.username, user.avatar, history.content, history.timestamp)
        } else {
          ChatHistory(history.id, u.username, u.avatar, history.content, history.timestamp)
        }
      }
    } else Nil

    val ret = new util.ArrayList[ChatHistory]()
    list.foreach(f => ret.add(f))
    ret
  }

  /** 查询离线消息
    *
    * @param uid    消息所属用户id、用户个人id
    * @param status 历史消息还是离线消息 0代表离线 1表示已读
    * @return List[Receive]
    */
  def findOffLineMessage(uid: Int, status: Int): util.List[Receive] =
    userRepository.findOffLineMessage(uid, status)

  /** 保存用户聊天记录
    *
    * @param receive 聊天记录信息
    * @see Receive.scala
    * @return Int
    */
  @Transactional
  def saveMessage(receive: Receive): Int = userRepository.saveMessage(receive)

  /** 用户更新签名
    *
    * @param user 消息所属用户、用户个人
    * @see User.scala
    * @return Boolean
    */
  @Transactional
  def updateSing(user: User): Boolean = {
    if (user == null || user.sign == null) false
    else userRepository.updateSign(user.sign, user.id) == 1
  }

  /** 激活码激活用户
    *
    * @param activeCode 激活码
    * @return Int
    */
  def activeUser(activeCode: String): Int = {
    if (activeCode == null || "".equals(activeCode)) 0
    else userRepository.activeUser(activeCode)
  }

  /** 判断邮件是否存在
    *
    * @param email 邮箱
    * @return Boolean
    */
  def existEmail(email: String): Boolean = {
    if (email == null || "".equals(email)) false
    else userRepository.matchUser(email) != null
  }

  /** 用户邮件和密码是否匹配
    *
    * @param user 用户
    * @see User.scala
    * @return User
    */
  def matchUser(user: User): User = {
    if (user == null || user.email == null) {
      null
    } else {
      val u: User = userRepository.matchUser(user.email)
      //密码不匹配
      if (u == null || !SecurityUtil.matched(user.password, u.password)) {
        null
      } else u
    }
  }

  /** 根据群组ID查询群里用户的信息
    *
    * @param gid 群组id
    * @return List[User]
    */
  @Cacheable(value = Array("findUserByGroupId"), keyGenerator = "wiselyKeyGenerator")
  def findUserByGroupId(gid: Int): util.List[User] = userRepository.findUserByGroupId(gid)

  /** 根据ID查询用户的好友分组的列表信息
    *
    * FriendList表示一个好友列表，一个用户可以有多个FriendList
    *
    * @param uid 用户ID
    * @return List[FriendList]
    */
  @Cacheable(value = Array("findFriendGroupsById"), keyGenerator = "wiselyKeyGenerator")
  def findFriendGroupsById(uid: Int): util.List[FriendList] = {
    val friends = userRepository.findFriendGroupsById(uid)
    val ret = new util.ArrayList[FriendList](friends.size())
    //封装分组列表下的好友信息
    friends.asScala
      .foreach { friend: FriendList =>
        ret.add(friend.copy(list = userRepository.findUsersByFriendGroupIds(friend.id)))
      }
    ret
  }

  /** 根据ID查询用户信息
    *
    * @param id 用户id
    * @return User
    */
  @Cacheable(value = Array("findUserById"), keyGenerator = "wiselyKeyGenerator")
  def findUserById(id: Int): User = userRepository.findUserById(id)

  /** 根据用户ID查询用户的群组列表
    *
    * @param id 用户id
    * @return List[GroupList]
    */
  @Cacheable(value = Array("findGroupsById"), keyGenerator = "wiselyKeyGenerator")
  def findGroupsById(id: Int): util.List[GroupList] = userRepository.findGroupsById(id)

  /** 保存用户信息
    *
    * @param user 用户
    * @see User.scala
    * @return Boolean
    */
  //清除缓存
  @CacheEvict(
    value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"),
    allEntries = true
  )
  @Transactional
  def saveUser(user: User, request: HttpServletRequest): Boolean = {
    if (user == null || user.username == null || user.password == null || user.email == null) {
      false
    } else {
      //激活码
      val activeCode = UUIDUtil.getUUID64String()
      val userCopy = user.copy(
        active = activeCode,
        createDate = DateUtil.getDate,
        password = SecurityUtil.encrypt(user.password)
      )
      userRepository.saveUser(userCopy)
      log.info("userid = " + userCopy.id)
      //创建默认的好友分组
      createFriendGroup(SystemConstant.DEFAULT_GROUP_NAME, userCopy.id)
      //发送激活电子邮件
      mailService.sendHtmlMail(
        userCopy.email,
        SystemConstant.SUBJECT,
        userCopy.username + ",请确定这是你本人注册的账号   " + ", " + WebUtil.getServerIpAdder(
          request
        ) + "/user/active/" + activeCode
      )
      true
    }
  }

}
