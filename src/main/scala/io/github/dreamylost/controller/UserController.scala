package io.github.dreamylost.controller

import com.github.pagehelper.PageHelper
import io.github.dreamylost.ResultPageSet
import io.github.dreamylost.ResultSet
import io.github.dreamylost.constant.SystemConstant
import io.github.dreamylost.model.domain.UserVo
import io.github.dreamylost.model.domain._
import io.github.dreamylost.model.entity.FriendGroup
import io.github.dreamylost.model.entity.GroupList
import io.github.dreamylost.model.entity.User
import io.github.dreamylost.service.CookieService
import io.github.dreamylost.service.UserService
import io.github.dreamylost.util.FileUtil
import io.github.dreamylost.util.SecurityUtil
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation._
import org.springframework.web.multipart.MultipartFile

import java.util
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import scala.collection.JavaConverters._

/**
  * 用户接口
  *
 * @date 2018年9月9日
  * @author 梦境迷离
  */
@Controller
@Api(value = "用户相关操作")
@RequestMapping(value = Array("/user"))
class UserController @Autowired() (userService: UserService, cookieService: CookieService) {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[UserController])

  /**
    * 退出群
    *
   * @param groupId 群编号
    * @param request
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/leaveOutGroup"))
  def leaveOutGroup(
      @RequestParam("groupId") groupId: Integer,
      request: HttpServletRequest
  ): ResultSet = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val result = userService.leaveOutGroup(groupId, user.id)
    if (result)
      ResultSet(code = SystemConstant.SUCCESS, msg = SystemConstant.SUCCESS_MESSAGE)
    else ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.LEAVEOUT_GROUP_ERROR)
  }

  /**
    * 删除好友
    *
   * @param friendId
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/removeFriend"))
  def removeFriend(
      @RequestParam("friendId") friendId: Integer,
      request: HttpServletRequest
  ): ResultSet = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val result = userService.removeFriend(friendId, user.id)
    ResultSet(result)
  }

  /**
    * 移动好友分组
    *
   * @param groupId 新的分组id
    * @param userId  被移动的好友id
    * @param request
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/changeGroup"))
  def changeGroup(
      @RequestParam("groupId") groupId: Integer,
      @RequestParam("userId") userId: Integer,
      request: HttpServletRequest
  ): ResultSet = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val result = userService.changeGroup(groupId, userId, user.id)
    if (result) ResultSet(result)
    else ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.ERROR_MESSAGE)
  }

  /**
    * 拒绝添加好友
    *
   * @param request
    * @param messageBoxId 消息盒子的消息id
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/refuseFriend"))
  def refuseFriend(
      @RequestParam("messageBoxId") messageBoxId: Integer,
      request: HttpServletRequest
  ): ResultSet = {
    val result = userService.updateAddMessage(messageBoxId, 2)
    ResultSet(result)
  }

  /**
    * 同意添加好友
    *
   * @param uid          对方用户ID
    * @param fromGroup    对方设定的好友分组
    * @param group        我设定的好友分组
    * @param messageBoxId 消息盒子的消息id
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/agreeFriend"))
  def agreeFriend(
      @RequestParam("uid") uid: Integer,
      @RequestParam("from_group") fromGroup: Integer,
      @RequestParam("group") group: Integer,
      @RequestParam("messageBoxId") messageBoxId: Integer,
      request: HttpServletRequest
  ): ResultSet = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val result = userService.addFriend(user.id, group, uid, fromGroup, messageBoxId)
    if (!result)
      ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.ERROR_ADD_REPETITION)
    else ResultSet(result)
  }

  /**
    * 查询消息盒子信息
    *
   * @param uid
    * @param page
    * @return String
    */
  @ResponseBody
  @GetMapping(Array("/findAddInfo"))
  def findAddInfo(@RequestParam("uid") uid: Int, @RequestParam("page") page: Int): ResultPageSet = {
    PageHelper.startPage(page, SystemConstant.ADD_MESSAGE_PAGE)
    val list = userService.findAddInfo(uid)
    val count = userService.countUnHandMessage(uid, null)
    val pages =
      if (count < SystemConstant.ADD_MESSAGE_PAGE) 1
      else count / SystemConstant.ADD_MESSAGE_PAGE + 1
    ResultPageSet(list, pages)
  }

  /**
    * 分页查找好友
    *
   * @param page 第几页
    * @param name 好友名字
    * @param sex  性别
    * @return String
    */
  @ResponseBody
  @GetMapping(Array("/findUsers"))
  def findUsers(
      @RequestParam(value = "page", defaultValue = "1") page: Int,
      @RequestParam(value = "name", required = false) name: String,
      @RequestParam(value = "sex", required = false) sex: Integer
  ): ResultPageSet = {
    val count = userService.countUsers(name, sex)
    LOGGER.info(s"find users => [total = $count]")
    val pages =
      if (count < SystemConstant.USER_PAGE) 1
      else {
        if (count % SystemConstant.USER_PAGE == 0) count / SystemConstant.USER_PAGE
        else count / SystemConstant.USER_PAGE + 1
      }
    PageHelper.startPage(page, SystemConstant.USER_PAGE)
    val users = userService.findUsers(name, sex)
    ResultPageSet(users, pages)
  }

  /**
    * 分页查找群组
    *
   * @param page 第几页
    * @param name 群名称
    * @return String
    */
  @ResponseBody
  @GetMapping(Array("/findGroups"))
  def findGroups(
      @RequestParam(value = "page", defaultValue = "1") page: Int,
      @RequestParam(value = "name", required = false) name: String
  ): ResultPageSet = {
    val count = userService.countGroup(name)
    val pages =
      if (count < SystemConstant.USER_PAGE) 1
      else {
        if (count % SystemConstant.USER_PAGE == 0) count / SystemConstant.USER_PAGE
        else count / SystemConstant.USER_PAGE + 1
      }
    PageHelper.startPage(page, SystemConstant.USER_PAGE)
    val groups = userService.findGroup(name)
    ResultPageSet(groups, pages)
  }

  /**
    * 分页查询我的创建的群组
    *
   * @param page
    * @param createId
    * @return
    */
  @ResponseBody
  @GetMapping(Array("/findMyGroups"))
  def findMyGroups(
      @RequestParam(value = "page", defaultValue = "1") page: Int,
      @RequestParam(value = "createId", required = true) createId: Int
  ): ResultPageSet = {
    val groups: util.List[GroupList] = userService.findGroupsById(createId)
    var result: ResultPageSet = null
    if (groups == null) {
      return result
    }
    val groupNews = groups.toArray.filter(x => x.asInstanceOf[GroupList].createId.equals(createId))
    result = ResultPageSet(groupNews, 0)
    val count = groupNews.length
    val pages =
      if (count < SystemConstant.USER_PAGE) 1
      else {
        if (count % SystemConstant.USER_PAGE == 0) count / SystemConstant.USER_PAGE
        else count / SystemConstant.USER_PAGE + 1
      }
    PageHelper.startPage(page, SystemConstant.USER_PAGE)
    result.copy(pages = pages)
  }

  /**
    * 获取聊天记录
    *
   * @param id   与谁的聊天记录id
    * @param `type` 类型，可能是friend或者是group
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/chatLog"))
  def chatLog(
      @RequestParam("id") id: Integer,
      @RequestParam("type") `type`: String,
      @RequestParam("page") page: Int,
      request: HttpServletRequest
  ): ResultSet = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    PageHelper.startPage(page, SystemConstant.SYSTEM_PAGE)
    //查找聊天记录
    val historys: util.List[ChatHistory] = userService.findHistoryMessage(user, id, `type`)
    ResultSet(historys)
  }

  /**
    * 弹出聊天记录页面
    *
   * @param id   与谁的聊天记录id
    * @param `type` 类型，可能是friend或者是group
    * @return String
    */
  @GetMapping(Array("/chatLogIndex"))
  def chatLogIndex(
      @RequestParam("id") id: Integer,
      @RequestParam("type") `type`: String,
      model: Model,
      request: HttpServletRequest
  ): String = {
    model.addAttribute("id", id)
    model.addAttribute("type", `type`)
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    var pages: Int = userService.countHistoryMessage(user.id, id, `type`)
    pages =
      if (pages < SystemConstant.SYSTEM_PAGE) pages else pages / SystemConstant.SYSTEM_PAGE + 1
    model.addAttribute("pages", pages)
    "chatLog"
  }

  /**
    * 获取离线消息
    *
   * @return String
    */
  @ResponseBody
  @PostMapping(Array("/getOffLineMessage"))
  def getOffLineMessage(request: HttpServletRequest): ResultSet = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    LOGGER.info(s"find offline msg [uid = ${user.id}]")
    val users = userService.findOffLineMessage(user.id, 0).asScala.map { receive =>
      val user = userService.findUserById(receive.id)
      user.copy(username = user.username, avatar = user.avatar)
    }
    ResultSet(users)
  }

  /**
    * 更新签名
    *
   * @param sign
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/updateSign"))
  def updateSign(request: HttpServletRequest, @RequestParam("sign") sign: String): ResultSet = {
    val user: User = request.getSession.getAttribute("user").asInstanceOf[User].copy(sign = sign)
    if (userService.updateSing(user)) ResultSet()
    else ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.ERROR_MESSAGE)
  }

  /**
    * 激活
    *
   * @param activeCode
    * @return String
    *
   */
  @GetMapping(Array("/active/{activeCode}"))
  def activeUser(@PathVariable("activeCode") activeCode: String): String = {
    if (userService.activeUser(activeCode) == 1)
      "redirect:/#tologin?status=1"
    else "redirect:/#toregister?status=0"
    //http://localhost/user/active/1ade893a1b1940a5bb8dc8447538a6a6a18ad80bcf84437a8cfb67213337202d
  }

  /**
    * 注册
    *
   * @param user
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/register"))
  def register(@RequestBody user: User, request: HttpServletRequest): ResultSet = {
    if (userService.saveUser(user, request))
      ResultSet(code = SystemConstant.SUCCESS, msg = SystemConstant.REGISTER_SUCCESS)
    else ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.REGISTER_FAIL)
  }

  /**
    * 登录
    *
   * @param user
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/login"))
  def login(
      @RequestBody user: User,
      request: HttpServletRequest,
      response: HttpServletResponse
  ): ResultSet = {
    val u: User = userService.matchUser(user)
    //未激活
    if (u != null && "nonactivated".equals(u.status)) {
      ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.NONACTIVED)
    } else if (u != null && !"nonactivated".equals(u.status)) {
      LOGGER.info(s"user login success => [user = $user, u = $u]")
      request.getSession.setAttribute("user", u)
      cookieService.addCookie(user, request, response)
      ResultSet(u)
    } else {
      val result = ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.LOGGIN_FAIL)
      result
    }
  }

  /**
    * 初始化主界面数据
    *
   * @param userId
    * @return String
    */
  @ResponseBody
  @ApiOperation("初始化聊天界面数据，分组列表好友信息、群列表")
  @PostMapping(Array("/init/{userId}"))
  def init(@PathVariable("userId") userId: Int): ResultSet = {
    //用户信息
    val user = userService.findUserById(userId)
    val data = FriendAndGroupInfo(
      mine = user.copy(status = "online"),
      friend = userService.findFriendGroupsById(userId).asScala.toList,
      group = userService.findGroupsById(userId).asScala.toList
    )
    ResultSet(data)
  }

  /**
    * 获取群成员
    *
   * @param id
    * @return String
    */
  @ResponseBody
  @GetMapping(Array("/getMembers"))
  def getMembers(@RequestParam("id") id: Int): ResultSet = {
    val users = userService.findUserByGroupId(id)
    val friends = FriendList(id = 0, groupname = null, list = users.asScala.toList)
    ResultSet(friends)
  }

  /**
    * 客户端上传图片
    *
   * @param file
    * @param request
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/upload/image"))
  def uploadImage(
      @RequestParam("file") file: MultipartFile,
      request: HttpServletRequest
  ): ResultSet = {
    if (file.isEmpty)
      ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.UPLOAD_FAIL)
    else {
      val path = request.getServletContext.getRealPath("/")
      val src = FileUtil.upload(SystemConstant.IMAGE_PATH, path, file)
      val result = new util.HashMap[String, String]
      //图片的相对路径地址
      result.put("src", src)
      LOGGER.info("图片" + file.getOriginalFilename + "上传成功")
      ResultSet(result)
    }
  }

  /**
    * 上传群组头像
    *
   * @param file
    * @param request
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/upload/groupAvatar"))
  def uploadGroupAvatar(
      @RequestParam("avatar") file: MultipartFile,
      request: HttpServletRequest
  ): ResultSet = {
    if (file.isEmpty)
      ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.UPLOAD_FAIL)
    else {
      val path = request.getServletContext.getRealPath("/")
      val src = FileUtil.upload(SystemConstant.GROUP_AVATAR_PATH, path, file)
      val result = new util.HashMap[String, String]
      //图片的相对路径地址
      result.put("src", src)
      LOGGER.info("图片" + file.getOriginalFilename + "上传成功")
      ResultSet(result)
    }
  }

  /**
    * 用户创建群组
    *
   * @param groupList 群组
    * @return String
    */
  @PostMapping(Array("/createGroup"))
  @ResponseBody
  def createGroup(@RequestBody groupList: GroupList): ResultSet = {
    val ret = userService.createGroup(groupList)
    if (ret == -1) {
      return ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.CREATE_GROUP_ERROR)
    }
    if (userService.addGroupMember(ret, groupList.createId)) {
      return ResultSet(code = SystemConstant.SUCCESS, msg = SystemConstant.CREATE_GROUP_SUCCCESS)
    }
    ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.CREATE_GROUP_ERROR)
  }

  /**
    * 用户创建好友分组
    *
   * @param friendGroup 好友分组
    * @return String
    */
  @PostMapping(Array("/createUserGroup"))
  @ResponseBody
  def createUserGroup(@RequestBody friendGroup: FriendGroup): ResultSet = {
    val ret = userService.createFriendGroup(friendGroup.groupname, friendGroup.uid)
    if (!ret) {
      return ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.CREATE_USER_GROUP_ERROR)
    }
    ResultSet(code = SystemConstant.SUCCESS, msg = SystemConstant.CREATE_USER_GROUP_SUCCCESS)
  }

  /**
    * 客户端上传文件
    *
   * @param file
    * @param request
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/upload/file"))
  def uploadFile(
      @RequestParam("file") file: MultipartFile,
      request: HttpServletRequest
  ): ResultSet = {
    if (file.isEmpty)
      ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.UPLOAD_FAIL)
    else {
      val path = request.getServletContext.getRealPath("/")
      val src = FileUtil.upload(SystemConstant.FILE_PATH, path, file)
      val result = new util.HashMap[String, String]
      //文件的相对路径地址
      result.put("src", src)
      result.put("name", file.getOriginalFilename)
      LOGGER.info("文件" + file.getOriginalFilename + "上传成功")
      ResultSet(result)
    }
  }

  /**
    * 用户更新头像
    *
   * @param avatar
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/updateAvatar"))
  def updateAvatar(
      @RequestParam("avatar") avatar: MultipartFile,
      request: HttpServletRequest
  ): ResultSet = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val path = request.getServletContext.getRealPath(SystemConstant.AVATAR_PATH)
    val src = FileUtil.upload(path, avatar)
    userService.updateAvatar(user.id, src)
    val result = new util.HashMap[String, String]
    result.put("src", src)
    ResultSet(result)
  }

  /**
    * 更新信息个人信息
    *
   * @param user
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/updateInfo"))
  def updateAvatar(@RequestBody user: UserVo): ResultSet = {
    if (user == null)
      ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.UPDATE_INFO_FAIL)
    else {
      val u = userService.findUserById(user.id)
      val sex = if (user.sex.equals("nan")) 1 else 0
      //前台明文传输，有安全问题
      if (
        user.password == null || user.password.trim.isEmpty ||
        user.oldpwd == null || user.oldpwd.trim.isEmpty
      ) {
        userService.updateUserInfo(u.copy(sex = sex, sign = user.sign, username = user.username))
        ResultSet(code = SystemConstant.SUCCESS, msg = SystemConstant.UPDATE_INFO_SUCCESS)
      } else if (!SecurityUtil.matchs(user.oldpwd, u.password)) {
        ResultSet(code = SystemConstant.ERROR, msg = SystemConstant.UPDATE_INFO_PASSWORD_FAIL)
      } else {
        userService.updateUserInfo(
          u.copy(
            password = SecurityUtil.encrypt(user.password),
            sex = sex,
            sign = user.sign,
            username = user.username
          )
        )
        ResultSet(code = SystemConstant.SUCCESS, msg = SystemConstant.UPDATE_INFO_SUCCESS)
      }
    }
  }

  /**
    * 跳转主页
    *
   * @param model
    * @param request
    * @return String
    */
  @GetMapping(Array("/index"))
  def index(model: Model, request: HttpServletRequest): String = {
    val user = request.getSession.getAttribute("user")
    model.addAttribute("user", user)
    LOGGER.info(s"user access server => [user = $user]")
    "index"
  }

  /**
    * 根据id查找用户信息
    *
   * @param id
    * @return String
    */
  @ResponseBody
  @GetMapping(Array("/findUser"))
  def findUserById(@RequestParam("id") id: Integer): ResultSet = {
    ResultSet(userService.findUserById(id))
  }

  /**
    * 判断邮件是否存在
    *
   * @param email
    * @return String
    */
  @ResponseBody
  @PostMapping(Array("/existEmail"))
  def existEmail(@RequestParam("email") email: String): ResultSet = {
    ResultSet(userService.existEmail(email))
  }

}
