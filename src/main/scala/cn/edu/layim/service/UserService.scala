package cn.edu.layim.service

import java.util.{ArrayList, List}

import cn.edu.layim.common.SystemConstant
import cn.edu.layim.domain.{AddInfo, FriendList, GroupList, GroupMember}
import cn.edu.layim.entity._
import cn.edu.layim.repository.UserMapper
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
class UserService @Autowired()(private var userMapper: UserMapper) {

    private final val LOGGER: Logger = LoggerFactory.getLogger(classOf[UserService])

    //电子邮件相关服务
    @Autowired private var mailService: MailService = _

    /**
      * 退出群
      *
      * @param gid
      * @param uid
      */
    @CacheEvict(value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"), allEntries = true)
    def leaveOutGroup(gid: Int, uid: Int): Boolean = userMapper.leaveOutGroup(new GroupMember(gid, uid)) == 1

    /**
      * 添加群成员
      *
      * @param gid          群编号
      * @param uid          用户编号
      * @param messageBoxId 消息盒子Id
      */
    @Transactional
    def addGroupMember(gid: Int, uid: Int, messageBoxId: Int): Boolean = {
        if (gid == null || uid == null) {
            return false
        } else {
            userMapper.addGroupMember(new GroupMember(gid, uid)) == 1
            updateAddMessage(messageBoxId, 1)
        }
    }

    /**
      * 删除好友
      *
      * @param friendId 好友Id
      * @param uId      个人Id
      * @return Boolean
      */
    @CacheEvict(value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"), allEntries = true)
    def removeFriend(friendId: Int, uId: Int): Boolean = {
        if (friendId == null || uId == null)
            return false
        else
            userMapper.removeFriend(friendId, uId) == 1
    }

    /**
      * 更新用户头像
      *
      * @param userId
      * @param avatar
      * @return
      */
    @CacheEvict(value = Array("findUserById"), allEntries = true)
    @Transactional
    def updateAvatar(userId: Int, avatar: String): Boolean = {
        if (userId == null | avatar == null)
            return false
        else
            userMapper.updateAvatar(userId, avatar) == 1
    }

    /**
      * 移动好友分组
      *
      * @param groupId 新的分组id
      * @param uId     被移动的好友id
      * @param mId     我的id
      * @return
      */
    //清除缓存
    @CacheEvict(value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"), allEntries = true)
    @Transactional
    def changeGroup(groupId: Int, uId: Int, mId: Int): Boolean = {
        if (groupId == null || uId == null || mId == null)
            return false
        else
            userMapper.changeGroup(groupId, uId, mId) == 1
    }

    /**
      * 添加好友操作
      *
      * @param mid          我的id
      * @param mgid         我设定的分组
      * @param tid          对方的id
      * @param tgid         对方设定的分组
      * @param messageBoxId 消息盒子的消息id
      */
    @Transactional
    @CacheEvict(value = Array("findUserById", "findFriendGroupsById", "findUserByGroupId"), allEntries = true)
    def addFriend(mid: Int, mgid: Int, tid: Int, tgid: Int, messageBoxId: Int): Boolean = {
        val add = new AddFriends(mid, mgid, tid, tgid)
        if (userMapper.addFriend(add) != 0) {
            updateAddMessage(messageBoxId, 1)
        }
        false
    }

    /**
      * 创建好友分组列表
      *
      * @param uid
      * @param groupName
      */
    def createFriendGroup(groupName: String, uid: Int): Boolean = {
        if (uid == null || groupName == null || "".equals(uid) || "".equals(groupName))
            return false
        else
            userMapper.createFriendGroup(new FriendGroup(uid, groupName)) == 1
    }

    /**
      * 统计消息
      *
      * @param uid
      * @param agree
      */
    def countUnHandMessage(uid: Int, agree: Integer): Int = userMapper.countUnHandMessage(uid, agree)

    /**
      * 查询添加好友、群组信息
      *
      * @param uid
      * @return List[AddInfo]
      */
    def findAddInfo(uid: Int): List[AddInfo] = {
        val list = userMapper.findAddInfo(uid)
        JavaConversions.collectionAsScalaIterable(list).foreach { info => {
            if (info.Type == 0) {
                info.setContent("申请添加你为好友")
            } else {
                val group: GroupList = userMapper.findGroupById(info.getFrom_group)
                info.setContent("申请加入 '" + group.getGroupName + "' 群聊中!")
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
      * @param messageBoxId
      * @param agree
      * @return
      */
    @Transactional
    def updateAddMessage(messageBoxId: Int, agree: Int): Boolean = {
        var addMessage = new AddMessage
        addMessage.setAgree(agree)
        addMessage.setId(messageBoxId)
        userMapper.updateAddMessage(addMessage) == 1
    }


    /**
      * 添加好友、群组信息请求
      *
      * @param addMessage
      * @return
      */
    def saveAddMessage(addMessage: AddMessage): Int = userMapper.saveAddMessage(addMessage)

    /**
      * 根据群名模糊统计
      *
      * @param groupName
      * @return
      */
    def countGroup(groupName: String): Int = userMapper.countGroup(groupName)

    /**
      * 根据群名模糊查询群
      *
      * @param groupName
      * @return
      */
    def findGroup(groupName: String): List[GroupList] = userMapper.findGroup(groupName)

    /**
      * 根据用户名和性别统计用户
      *
      * @param username
      * @param sex
      */
    def countUsers(username: String, sex: Int): Int = userMapper.countUser(username, sex)

    /**
      * 根据用户名和性别查询用户
      *
      * @param username
      * @param sex
      */
    def findUsers(username: String, sex: Int): List[User] = userMapper.findUsers(username, sex)


    /**
      * 统计查询消息
      *
      * @param uid  消息所属用户
      * @param mid  来自哪个用户
      * @param Type 消息类型，可能来自friend或者group
      */
    def countHistoryMessage(uid: Int, mid: Int, Type: String): Int = {
        Type match {
            case "friend" => userMapper.countHistoryMessage(uid, mid, Type)
            case "group" => userMapper.countHistoryMessage(null, mid, Type)
        }
    }

    /**
      * 查询历史消息
      *
      * @param user
      * @param mid
      * @param Type
      */
    def findHistoryMessage(user: User, mid: Int, Type: String): List[ChatHistory] = {
        val list = new ArrayList[ChatHistory]()
        //单人聊天记录
        if ("friend".equals(Type)) {
            //查找聊天记录
            val historys: List[Receive] = userMapper.findHistoryMessage(user.getId, mid, Type)
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
            val historys: List[Receive] = userMapper.findHistoryMessage(null, mid, Type)
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
      * @param uid
      * @param status 历史消息还是离线消息 0代表离线 1表示已读
      */
    def findOffLineMessage(uid: Int, status: Int): List[Receive] = userMapper.findOffLineMessage(uid, status)


    /**
      * 保存用户聊天记录
      *
      * @param receive 聊天记录信息
      * @return Int
      */
    def saveMessage(receive: Receive): Int = userMapper.saveMessage(receive)

    /**
      * 用户更新签名
      *
      * @param user
      * @return Boolean
      */
    def updateSing(user: User): Boolean = {
        if (user == null || user.getSign == null || user.getId == null) {
            return false
        } else {
            return userMapper.updateSign(user.getSign, user.getId) == 1
        }
    }

    /**
      * 激活码激活用户
      *
      * @param activeCode
      * @return Int
      */
    def activeUser(activeCode: String): Int = {
        if (activeCode == null || "".equals(activeCode)) {
            return 0
        }
        userMapper.activeUser(activeCode)
    }

    /**
      * 判断邮件是否存在
      *
      * @param email
      * @return
      */
    def existEmail(email: String): Boolean = {
        if (email == null || "".equals(email))
            return false
        else
            userMapper.matchUser(email) != null
    }

    /**
      * 用户邮件和密码是否匹配
      *
      * @param user
      * @return User
      */
    def matchUser(user: User): User = {
        if (user == null || user.getEmail == null) {
            return null
        }
        val u: User = userMapper.matchUser(user.getEmail)
        //密码不匹配
        if (u == null || !SecurityUtil.matchs(user.getPassword, u.getPassword)) {
            return null
        }
        u
    }

    /**
      * 根据群组ID查询群里用户的信息
      *
      * @param gid
      * @return List[User]
      */
    @Cacheable(value = Array("findUserByGroupId"), keyGenerator = "wiselyKeyGenerator")
    def findUserByGroupId(gid: Int): List[User] = userMapper.findUserByGroupId(gid)

    /**
      * 根据ID查询用户好友分组列表信息
      *
      * @param uid 用户ID
      * @return List[FriendList]
      */
    @Cacheable(value = Array("findFriendGroupsById"), keyGenerator = "wiselyKeyGenerator")
    def findFriendGroupsById(uid: Int): List[FriendList] = {
        var friends = userMapper.findFriendGroupsById(uid)
        //封装分组列表下的好友信息
        JavaConversions.collectionAsScalaIterable(friends).foreach {
            friend: FriendList => {
                friend.list = userMapper.findUsersByFriendGroupIds(friend.getId)
            }
        }
        friends
    }

    /**
      * 根据ID查询用户信息
      *
      * @param id
      * @return User
      */
    @Cacheable(value = Array("findUserById"), keyGenerator = "wiselyKeyGenerator")
    def findUserById(id: Int): User = {
        if (id != null) userMapper.findUserById(id) else null
    }

    /**
      * 根据ID查询用户群组信息
      *
      * @param id
      * @return List[Group]
      */
    @Cacheable(value = Array("findGroupsById"), keyGenerator = "wiselyKeyGenerator")
    def findGroupsById(id: Int): List[GroupList] = {
        userMapper.findGroupsById(id)
    }

    /**
      * 保存用户信息
      *
      * @param user
      * @return Int
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
            userMapper.saveUser(user)
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