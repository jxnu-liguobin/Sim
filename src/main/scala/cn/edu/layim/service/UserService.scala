package cn.edu.layim.service

import java.util.{ArrayList, List}

import cn.edu.layim.common.SystemConstant
import cn.edu.layim.domain._
import cn.edu.layim.entity._
import cn.edu.layim.repository.UserRepository
import cn.edu.layim.util.{DateUtil, SecurityUtil, UUIDUtil, WebUtil}
import javax.servlet.http.HttpServletRequest
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.{CacheEvict, Cacheable}
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConversions

/**
  * 用户信息相关操作
  *
  * @date 2018年9月9日
  * @author 梦境迷离
  *
  */
@Service
class UserService @Autowired()(private var userRepository: UserRepository) {

    private final val LOGGER: Logger = LoggerFactory.getLogger(classOf[UserService])

    //邮件服务
    @Autowired
    private var mailService: MailService = _

    /**
      * 退出群
      *
      * @param gid 群组id
      * @param uid 用户
      * @return Boolean
      */
    @CacheEvict(value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"), allEntries = true)
    def leaveOutGroup(gid: Int, uid: Int): Boolean = userRepository.leaveOutGroup(new GroupMember(gid, uid)) == 1

    /**
      * 添加群成员
      *
      * @param gid          群组id
      * @param uid          用户id
      * @param messageBoxId 消息盒子Id
      * @return Boolean
      */
    @Transactional
    def addGroupMember(gid: Int, uid: Int, messageBoxId: Int): Boolean = {
        if (gid == null || uid == null) {
            return false
        } else {
            userRepository.addGroupMember(new GroupMember(gid, uid)) == 1
            updateAddMessage(messageBoxId, 1)
        }
    }

    /**
      * 删除好友
      *
      * @param friendId 好友id
      * @param uId      个人/用户id
      * @return Boolean
      */
    @CacheEvict(value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"), allEntries = true)
    def removeFriend(friendId: Int, uId: Int): Boolean = {
        if (friendId == null || uId == null) {
            return false
        }
        else {
            userRepository.removeFriend(friendId, uId) == 1
        }
    }

    /**
      * 更新用户头像
      *
      * @param userId 个人id
      * @param avatar 头像
      * @return Boolean
      */
    @CacheEvict(value = Array("findUserById"), allEntries = true)
    @Transactional
    def updateAvatar(userId: Int, avatar: String): Boolean = {
        if (userId == null | avatar == null) {
            return false
        }
        else {
            userRepository.updateAvatar(userId, avatar) == 1
        }
    }

    /**
      * 移动好友分组
      *
      * @param groupId 新的分组id
      * @param uId     被移动的好友id
      * @param mId     我的id
      * @return Boolean
      */
    //清除缓存
    @CacheEvict(value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"), allEntries = true)
    @Transactional
    def changeGroup(groupId: Int, uId: Int, mId: Int): Boolean = {
        if (groupId == null || uId == null || mId == null) {
            return false
        }
        else {
            userRepository.changeGroup(groupId, uId, mId) == 1
        }
    }

    /**
      * 添加好友操作
      *
      * @param mid          我的id
      * @param mgid         我设定的分组
      * @param tid          对方的id
      * @param tgid         对方设定的分组
      * @param messageBoxId 消息盒子的消息id
      * @return Boolean
      */
    @Transactional
    @CacheEvict(value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"), allEntries = true)
    def addFriend(mid: Int, mgid: Int, tid: Int, tgid: Int, messageBoxId: Int): Boolean = {
        val add = new AddFriends(mid, mgid, tid, tgid)
        if (userRepository.addFriend(add) != 0) {
            return updateAddMessage(messageBoxId, 1)
        }
        false
    }

    /**
      * 创建好友分组列表
      *
      * @param uid       个人id
      * @param groupName 群组id
      * @return BooleanFriendGroup
      */
    def createFriendGroup(groupName: String, uid: Int): Boolean = {
        if (uid == null || groupName == null || "".equals(uid) || "".equals(groupName)) {
            return false
        }
        else {
            userRepository.createFriendGroup(new FriendGroup(uid, groupName)) == 1
        }
    }

    /**
      * 统计消息
      *
      * @param uid   个人id
      * @param agree 0未处理，1同意，2拒绝
      * @return Int
      */
    def countUnHandMessage(uid: Int, agree: Integer): Int = userRepository.countUnHandMessage(uid, agree)

    /**
      * 查询添加好友、群组信息
      *
      * @param uid 个人id
      * @return List[AddInfo]
      */
    def findAddInfo(uid: Int): List[AddInfo] = {
        val list = userRepository.findAddInfo(uid)
        JavaConversions.collectionAsScalaIterable(list).foreach { info => {
            if (info.Type == 0) {
                info.setContent("申请添加你为好友")
            } else {
                val group: GroupList = userRepository.findGroupById(info.getFrom_group)
                info.setContent("申请加入 '" + group.getGroupname + "' 群聊中!")
            }
            info.setHref(null)
            info.setUser(findUserById(info.getFrom))
            LOGGER.info(info.toString())
        }
        }
        list
    }

    /**
      * 更新好友、群组信息请求
      *
      * @param messageBoxId 消息盒子id
      * @param agree        0未处理，1同意，2拒绝
      * @return Boolean
      */
    @Transactional
    def updateAddMessage(messageBoxId: Int, agree: Int): Boolean = {
        val addMessage = new AddMessage
        addMessage.setAgree(agree)
        addMessage.setId(messageBoxId)
        userRepository.updateAddMessage(addMessage) == 1
    }


    /**
      * 添加好友、群组信息请求
      *
      * @param addMessage 添加好友、群组信息对象
      * @see AddMessage.scala
      * @return Int
      */
    def saveAddMessage(addMessage: AddMessage): Int = userRepository.saveAddMessage(addMessage)

    /**
      * 根据群名模糊统计
      *
      * @param groupName 群组名称
      * @return Int
      */
    def countGroup(groupName: String): Int = userRepository.countGroup(groupName)

    /**
      * 根据群名模糊查询群
      *
      * @param groupName 群组名称
      * @return List[GroupList]
      */
    def findGroup(groupName: String): List[GroupList] = userRepository.findGroup(groupName)

    /**
      * 根据用户名和性别统计用户
      *
      * @param username 用户名
      * @param sex      性别
      * @return Int
      */
    def countUsers(username: String, sex: Integer): Int = userRepository.countUser(username, sex)

    /**
      * 根据用户名和性别查询用户
      *
      * @param username 用户名
      * @param sex      性别
      * @return List[User]
      */
    def findUsers(username: String, sex: Integer): List[User] = userRepository.findUsers(username, sex)


    /**
      * 统计查询消息
      *
      * @param uid  消息所属用户id、用户个人id
      * @param mid  来自哪个用户
      * @param Type 消息类型，可能来自friend或者group
      * @return Int
      */
    def countHistoryMessage(uid: Int, mid: Int, Type: String): Int = {
        Type match {
            case "friend" => userRepository.countHistoryMessage(uid, mid, Type)
            case "group" => userRepository.countHistoryMessage(null, mid, Type)
        }
    }

    /**
      * 查询历史消息
      *
      * @param user 所属用户、用户个人
      * @param mid  来自哪个用户
      * @param Type 消息类型，可能来自friend或者group
      * @see User.scala
      * @return List[ChatHistory]
      */
    def findHistoryMessage(user: User, mid: Int, Type: String): List[ChatHistory] = {
        val list = new ArrayList[ChatHistory]()
        //单人聊天记录
        if ("friend".equals(Type)) {
            //查找聊天记录
            val historys: List[Receive] = userRepository.findHistoryMessage(user.getId, mid, Type)
            val toUser = findUserById(mid)
            JavaConversions.collectionAsScalaIterable(historys).foreach { history => {
                var chatHistory: ChatHistory = null
                if (history.getId == mid) {
                    chatHistory = new ChatHistory(history.getId, toUser.getUsername, toUser.getAvatar, history.getContent, history.getTimestamp)
                } else {
                    chatHistory = new ChatHistory(history.getId, user.getUsername, user.getAvatar, history.getContent, history.getTimestamp)
                }
                list.add(chatHistory)
            }
            }
        }
        //群聊天记录
        if ("group".equals(Type)) {
            //查找聊天记录
            val historys: List[Receive] = userRepository.findHistoryMessage(null, mid, Type)
            JavaConversions.collectionAsScalaIterable(historys).foreach { history => {
                var chatHistory: ChatHistory = null
                val u = findUserById(history.getFromid)
                if (history.getFromid().equals(user.getId)) {
                    chatHistory = new ChatHistory(user.getId, user.getUsername, user.getAvatar, history.getContent, history.getTimestamp)
                } else {
                    chatHistory = new ChatHistory(history.getId, u.getUsername, u.getAvatar, history.getContent, history.getTimestamp)
                }
                list.add(chatHistory)
            }
            }
        }
        return list
    }

    /**
      * 查询离线消息
      *
      * @param uid    消息所属用户id、用户个人id
      * @param status 历史消息还是离线消息 0代表离线 1表示已读
      * @return List[Receive]
      */
    def findOffLineMessage(uid: Int, status: Int): List[Receive] = userRepository.findOffLineMessage(uid, status)


    /**
      * 保存用户聊天记录
      *
      * @param receive 聊天记录信息
      * @see Receive.scala
      * @return Int
      */
    def saveMessage(receive: Receive): Int = userRepository.saveMessage(receive)

    /**
      * 用户更新签名
      *
      * @param user 消息所属用户、用户个人
      * @see User.scala
      * @return Boolean
      */
    def updateSing(user: User): Boolean = {
        if (user == null || user.getSign == null || user.getId == null) {
            return false
        } else {
            return userRepository.updateSign(user.getSign, user.getId) == 1
        }
    }

    /**
      * 激活码激活用户
      *
      * @param activeCode 激活码
      * @return Int
      */
    def activeUser(activeCode: String): Int = {
        if (activeCode == null || "".equals(activeCode)) {
            return 0
        }
        userRepository.activeUser(activeCode)
    }

    /**
      * 判断邮件是否存在
      *
      * @param email 邮箱
      * @return Boolean
      */
    def existEmail(email: String): Boolean = {
        if (email == null || "".equals(email)) {
            return false
        }
        else {
            userRepository.matchUser(email) != null
        }
    }

    /**
      * 用户邮件和密码是否匹配
      *
      * @param user 用户
      * @see User.scala
      * @return User
      */
    def matchUser(user: User): User = {
        if (user == null || user.getEmail == null) {
            return null
        }
        val u: User = userRepository.matchUser(user.getEmail)
        //密码不匹配
        if (u == null || !SecurityUtil.matchs(user.getPassword, u.getPassword)) {
            return null
        }
        u
    }

    /**
      * 根据群组ID查询群里用户的信息
      *
      * @param gid 群组id
      * @return List[User]
      */
    @Cacheable(value = Array("findUserByGroupId"), keyGenerator = "wiselyKeyGenerator")
    def findUserByGroupId(gid: Int): List[User] = userRepository.findUserByGroupId(gid)

    /**
      * 根据ID查询用户的好友分组的列表信息
      *
      * FriendList表示一个好友列表，一个用户可以有多个FriendList
      *
      * @param uid 用户ID
      * @return List[FriendList]
      */
    @Cacheable(value = Array("findFriendGroupsById"), keyGenerator = "wiselyKeyGenerator")
    def findFriendGroupsById(uid: Int): List[FriendList] = {
        val friends = userRepository.findFriendGroupsById(uid)
        //封装分组列表下的好友信息
        JavaConversions.collectionAsScalaIterable(friends).foreach {
            friend: FriendList => {
                friend.list = userRepository.findUsersByFriendGroupIds(friend.getId)
            }
        }
        friends
    }

    /**
      * 根据ID查询用户信息
      *
      * @param id 用户id
      * @return User
      */
    @Cacheable(value = Array("findUserById"), keyGenerator = "wiselyKeyGenerator")
    def findUserById(id: Int): User = {
        if (id != null) {
            return userRepository.findUserById(id)
        } else {
            null
        }
    }

    /**
      * 根据ID查询群组列表
      *
      * @param id 群组id
      * @return List[GroupList]
      */
    @Cacheable(value = Array("findGroupsById"), keyGenerator = "wiselyKeyGenerator")
    def findGroupsById(id: Int): List[GroupList] = {
        userRepository.findGroupsById(id)
    }

    /**
      * 保存用户信息
      *
      * @param user 用户
      * @see User.scala
      * @return Boolean
      */
    //清除缓存
    @CacheEvict(value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"), allEntries = true)
    @Transactional
    def saveUser(user: User, request: HttpServletRequest): Boolean = {
        if (user == null || user.getUsername == null || user.getPassword == null || user.getEmail == null) {
            return false
        } else {
            //激活码
            val activeCode = UUIDUtil.getUUID64String
            user.setActive(activeCode)
            user.setCreateDate(DateUtil.getDate)
            //加密密码
            user.setPassword(SecurityUtil.encrypt(user.getPassword))
            userRepository.saveUser(user)
            LOGGER.info("userid = " + user.getId)
            //创建默认的好友分组
            createFriendGroup(SystemConstant.DEFAULT_GROUP_NAME, user.getId)
            //发送激活电子邮件
            mailService.sendHtmlMail(user.getEmail, SystemConstant.SUBJECT,
                user.getUsername + ",请确定这是你本人注册的账号   " + ", " + WebUtil.getServerIpAdder(request) + "/user/active/" + activeCode)
        }
        true
    }

}