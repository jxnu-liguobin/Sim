package io.github.dreamylost.repository

import io.github.dreamylost.model.domains.AddFriends
import io.github.dreamylost.model.domains.AddInfo
import io.github.dreamylost.model.domains.FriendList
import io.github.dreamylost.model.domains.Receive
import io.github.dreamylost.model.entities.AddMessage
import io.github.dreamylost.model.entities.FriendGroup
import io.github.dreamylost.model.entities.GroupList
import io.github.dreamylost.model.entities.GroupMember
import io.github.dreamylost.model.entities.User
import org.apache.ibatis.annotations._

import java.util

/** User Dao
  *
  * @since 2018年9月8日
  * @author 梦境迷离
  */
trait UserRepository {

  /** 创建群
    *
    * @param groupList 群组对象
    * @return Int
    */
  @Insert(
    Array(
      "insert into t_group(group_name,avatar,create_id) values(#{groupname},#{avatar},#{createId})"
    )
  )
  @SelectKey(
    before = false,
    statement = Array(" SELECT LAST_INSERT_ID() AS id "),
    resultType = classOf[Integer],
    keyProperty = "id",
    keyColumn = "id"
  )
  def createGroupList(groupList: GroupList): Int

  /** 删除群
    *
    * @param id 群id
    * @return
    */
  @Delete(Array("delete from t_group where id = #{id}"))
  def deleteGroup(id: Int): Int

  /** 退出群
    *
    * @param groupMember 群成员对象
    * @see GroupMember.scala
    * @return Int
    */
  @Delete(Array("delete from t_group_members where gid=#{gid} and uid=#{uid}"))
  def leaveOutGroup(groupMember: GroupMember): Int

  /** 添加群成员
    *
    * @param groupMember 群成员对象
    * @see GroupMember.scala
    * @return Int
    */
  @Insert(Array("insert into t_group_members(gid,uid) values(#{gid},#{uid})"))
  def addGroupMember(groupMember: GroupMember): Int

  /** 删除好友
    *
    * @param friendId 好友Id
    * @param uId      个人Id
    * @return Int
    */
  @Delete(
    Array(
      "delete from t_friend_group_friends where fgid in (select id from t_friend_group where uid in (#{friendId}, #{uId})) and uid in(#{friendId}, #{uId})"
    )
  )
  def removeFriend(@Param("friendId") friendId: Int, @Param("uId") uId: Int): Int

  /** 更新用户头像
    *
    * @param userId 用户id
    * @param avatar 用户头像
    * @return Int
    */
  @Update(Array("update t_user set avatar=#{avatar} where id=#{userId}"))
  def updateAvatar(@Param("userId") userId: Int, @Param("avatar") avatar: String): Int

  /** 移动好友分组
    *
    * @param groupId 新的分组id
    * @param originRecordId  原记录t_friend_group_friends的ID
    * @return Int
    */
  @Update(
    Array(
      "update t_friend_group_friends set fgid = #{groupId} where id = #{originRecordId}"
    )
  )
  def changeGroup(
      @Param("groupId") groupId: Int,
      @Param("originRecordId") originRecordId: Int
  ): Int

  /** 查询我的好友的分组
    * @param uId
    * @param mId
    * @return
    */
  @Select(
    Array(
      "select t.id from (select id from t_friend_group_friends where fgid in (select id from t_friend_group where uid = #{mId}) and uid = #{uId}) t"
    )
  )
  def findUserGroup(@Param("uId") uId: Int, @Param("mId") mId: Int): Integer

  /** 添加好友操作
    *
    * @param addFriends 添加朋友对象
    * @see AddFriends.scala
    * @return Int
    */
  @Insert(
    Array("insert into t_friend_group_friends(fgid,uid) values(#{mgid},#{tid}),(#{tgid},#{mid})")
  )
  def addFriend(addFriends: AddFriends): Int

  /** 统计未处理的消息
    *
    * @param uid
    * @param agree
    * @return Int
    */
  @Select(
    Array(
      "<script> select count(*) from t_add_message where to_uid=#{uid} <if test='agree!=null'> and agree=#{agree} </if> </script>"
    )
  )
  def countUnHandMessage(@Param("uid") uid: Int, @Param("agree") agree: Integer): Int

  /** 查询添加好友、群组信息
    *
    * @param uid
    * @return List[AddInfo]
    */
  @Select(Array("select * from t_add_message where to_uid = #{uid} order by time desc"))
  @Results(
    value = Array(
      new Result(property = "from", column = "from_uid"),
      new Result(property = "uid", column = "to_uid"),
      new Result(property = "read", column = "agree"),
      new Result(property = "from_group", column = "group_id")
    )
  )
  def findAddInfo(@Param("uid") uid: Int): util.List[AddInfo]

  /** 更新好友、群组信息请求
    *
    * @param addMessage 添加好友、群组信息对象
    * @see AddMessage.scala
    * @return Int
    */
  @Update(Array("update t_add_message set agree = #{agree} where id = #{id}"))
  def updateAddMessage(addMessage: AddMessage): Int

  /** 置为已读
    *
    * @param mine 我的id（群组id）
    * @param to   对方的id
    * @param typ  消息类型
    * @return Int
    */
  @Update(
    Array(
      "update t_message set status = 1 where status = 0 and mid = #{mine} and toid =#{to} and type = #{typ}"
    )
  )
  def readMessage(@Param("mine") mine: Int, @Param("to") to: Int, @Param("typ") typ: String): Int

  /** 添加好友、群组信息请求
    * ON DUPLICATE KEY UPDATE 首先这个语法的目的是为了解决重复性，当数据库中存在某个记录时，执行这条语句会更新它，而不存在这条记录时，会插入它。
    *
    * @param addMessage 添加好友、群组信息对象
    * @see AddMessage.scala
    * @return Int
    */
  @Insert(
    Array(
      "insert into t_add_message(from_uid,to_uid,group_id,remark,agree,type,time) values (#{fromUid},#{toUid},#{groupId},#{remark},#{agree},#{type},#{time}) ON DUPLICATE KEY UPDATE remark=#{remark},time=#{time},agree=#{agree};"
    )
  )
  def saveAddMessage(addMessage: AddMessage): Int

  /** 根据群名模糊统计
    *
    * @param groupName 群组名
    * @return Int
    */
  @Select(
    Array(
      "<script> select count(*) from t_group where 1 = 1 <if test='groupName != null'> and group_name like '%${groupName}%'</if></script>"
    )
  )
  def countGroup(@Param("groupName") groupName: String): Int

  /** 根据群名模糊查询群
    *
    * @param groupName 群组名
    * @return Int
    */
  @Select(
    Array(
      "<script> select id,group_name,avatar,create_id from t_group where 1 = 1 <if test='groupName != null'> and group_name like '%${groupName}%'</if></script>"
    )
  )
  def findGroup(@Param("groupName") groupName: String): util.List[GroupList]

  /** 根据群id查询群信息
    *
    * @param gid 群组id
    * @return GroupList
    */
  @Select(Array("select id,group_name,avatar,create_id from t_group where id = #{gid}"))
  def findGroupById(@Param("gid") gid: Int): GroupList

  /** 根据用户名和性别统计用户
    *
    * @param username 用户名
    * @param sex      性别
    * @return Int
    */
  @Select(
    Array(
      "<script> select count(*) from t_user where 1 = 1 <if test='username != null'> and username like '%${username}%'</if><if test='sex != null'> and sex=#{sex}</if></script>"
    )
  )
  def countUser(@Param("username") username: String, @Param("sex") sex: Integer): Int

  /** 根据用户名和性别查询用户
    *
    * @param username 用户名
    * @param sex      性别
    * @return List[User]
    */
  @Select(
    Array(
      "<script> select id,username,sex,status,sign,avatar,email from t_user where 1=1 <if test='username != null'> and username like '%${username}%'</if><if test='sex != null'> and sex=#{sex}</if></script>"
    )
  )
  def findUsers(@Param("username") username: String, @Param("sex") sex: Integer): util.List[User]

  /** 统计查询消息
    *
    * @param uid  消息所属用户
    * @param mid  来自哪个用户
    * @param Type 消息类型，可能来自friend或者group
    * @return Int
    */
  @Select(
    Array(
      "<script> select count(*) from t_message where type = #{type} and " +
        "<choose><when test='uid!=null and mid !=null'>(toid = #{uid} and mid = #{mid}) or (toid = #{mid} and mid = #{uid}) </when><when test='mid != null'> mid = #{mid} </when></choose> order by timestamp </script>"
    )
  )
  def countHistoryMessage(
      @Param("uid") uid: Integer,
      @Param("mid") mid: Int,
      @Param("type") `type`: String
  ): Int

  /** 查询消息
    *
    * @param uid  消息所属用户
    * @param mid  来自哪个用户
    * @param Type 消息类型，可能来自friend或者group
    * @return List[Receive]
    */
  @Results(value = Array(new Result(property = "id", column = "mid")))
  @Select(
    Array(
      "<script> select toid,fromid,mid,content,type,timestamp,status from t_message where type = #{type} and " +
        "<choose><when test='uid!=null and mid !=null'>(toid = #{uid} and mid = #{mid}) or (toid = #{mid} and mid = #{uid}) </when><when test='mid != null'> mid = #{mid} </when></choose> order by timestamp </script>"
    )
  )
  def findHistoryMessage(
      @Param("uid") uid: Integer,
      @Param("mid") mid: Int,
      @Param("type") `type`: String
  ): util.List[Receive]

  /** 查询消息
    *
    * @param uid
    * @param status 历史消息还是离线消息 0代表离线 1表示已读
    * @return List[Receive]
    */
  @Results(value = Array(new Result(property = "id", column = "mid")))
  @Select(
    Array(
      "select toid,fromid,mid,content,type,timestamp,status from t_message where toid = #{uid} and status = #{status}"
    )
  )
  def findOffLineMessage(@Param("uid") uid: Int, @Param("status") status: Int): util.List[Receive]

  /** 保存用户聊天记录
    *
    * @param receive 聊天记录信息
    * @see Receive.scala
    * @return Int
    */
  @Insert(
    Array(
      "insert into t_message(mid,toid,fromid,content,type,timestamp,status) values(#{id},#{toid},#{fromid},#{content},#{type},#{timestamp},#{status})"
    )
  )
  def saveMessage(receive: Receive): Int

  /** 更新签名
    *
    * @param sign 签名
    * @param uid  用户id
    * @return Int
    */
  @Update(Array("update t_user set sign = #{sign} where id = #{uid}"))
  def updateSign(@Param("sign") sign: String, @Param("uid") uid: Int): Int

  /** 更新用户信息
    *
    * @param user 用户
    * @return Int
    */
  @Update(
    Array(
      "update t_user set username= #{username}, sex = #{sex}, sign = #{sign}, password = #{password} where id = #{id}"
    )
  )
  def updateUserInfo(user: User): Int

  /** 更新用户状态
    *
    * @param user 用户
    * @return Int
    */
  @Update(
    Array(
      "update t_user set status = #{status} where id = #{id}"
    )
  )
  def updateUserStatus(user: User): Int

  /** 激活用户账号
    *
    * @param activeCode 激活码
    * @return List[User]
    */
  @Update(Array("update t_user set status = 'offline' where active = #{activeCode}"))
  def activeUser(@Param("activeCode") activeCode: String): Int

  /** 根据群组ID查询群里用户的信息
    *
    * @param gid 群组id
    * @return List[User]
    */
  @Select(
    Array(
      "select id,username,status,sign,avatar,email from t_user where id in(select uid from t_group_members where gid = #{gid})"
    )
  )
  def findUserByGroupId(gid: Int): util.List[User]

  /** 根据ID查询用户信息
    *
    * @param id 用户id
    * @return User
    */
  @Select(
    Array(
      "select id,username,password,status,sign,avatar,email,sex,create_date from t_user where id = #{id}"
    )
  )
  def findUserById(id: Int): User

  /** 根据ID查询用户群组列表，不管是自己创建的还是别人创建的
    *
    * @param uid 用户ID
    * @return List[GroupList]
    */
  @Results(value = Array(new Result(property = "createId", column = "create_id")))
  @Select(
    Array(
      "select id,group_name,avatar,create_id from t_group where id in(select distinct gid from t_group_members where uid = #{uid})"
    )
  )
  def findGroupsById(uid: Int): util.List[GroupList]

  /** 根据ID查询该用户的好友分组的列表
    *
    * @param uid 用户ID
    * @return List[FriendList]
    */
  @Select(Array("select id, group_name from t_friend_group where uid = #{uid}"))
  def findFriendGroupsById(uid: Int): util.List[FriendList]

  /** 根据好友列表ID查询用户信息列表
    *
    * @param fgid 好友分组id
    * @return List[User]
    */
  @Select(
    Array(
      "select id,username,avatar,sign,status,email,sex from t_user where id in(select uid from t_friend_group_friends where fgid = #{fgid})"
    )
  )
  def findUsersByFriendGroupIds(fgid: Int): util.List[User]

  /** 保存用户信息
    *
    * @param user 用户对象
    * @see User.scala
    * @return Int 新增用户id
    */
  @Insert(
    Array(
      "insert into t_user(username,password,email,create_date,active) values(#{username},#{password},#{email},#{createDate},#{active})"
    )
  )
  @Options(useGeneratedKeys = true, keyProperty = "id")
  def saveUser(user: User): Int = user.id

  /** 根据邮箱匹配用户
    *
    * @param email 邮箱
    * @return User
    */
  @Select(
    Array(
      "select id,username,email,avatar,sex,sign,password,status,active,create_date from t_user where email = #{email}"
    )
  )
  def matchUser(email: String): User

  /** 创建好友分组记录
    *
    * @param friendGroup 好友分组
    * @return Int
    */
  @Insert(Array("insert into t_friend_group(group_name,uid) values(#{groupname},#{uid})"))
  def createFriendGroup(friendGroup: FriendGroup): Int

}
