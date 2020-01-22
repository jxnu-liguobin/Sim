package cn.edu.layim.controller

import java.util
import java.util.List

import cn.edu.layim.constant.SystemConstant
import cn.edu.layim.domain._
import cn.edu.layim.entity.{ GroupList, User }
import cn.edu.layim.service.{ CookieService, RedisService, UserService }
import cn.edu.layim.util.{ FileUtil, SecurityUtil }
import com.github.pagehelper.PageHelper
import com.google.gson.Gson
import io.swagger.annotations.{ Api, ApiOperation }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.slf4j.{ Logger, LoggerFactory }
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation._
import org.springframework.web.multipart.MultipartFile

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
class UserController @Autowired()(userService: UserService, redisService: RedisService, cookieService: CookieService) {

  private final lazy val LOGGER: Logger = LoggerFactory.getLogger(classOf[UserController])

  //可省略
  private final lazy val gson: Gson = new Gson

  /**
   * 退出群
   *
   * @param groupId 群编号
   * @param request
   * @return String
   */
  @ResponseBody
  @PostMapping(Array("/leaveOutGroup"))
  def leaveOutGroup(@RequestParam("groupId") groupId: Integer, request: HttpServletRequest): String = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val result = userService.leaveOutGroup(groupId, user.getId)
    if (result) gson.toJson(new ResultSet(SystemConstant.SUCCESS, SystemConstant.SUCCESS_MESSAGE))
    else gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.LEAVEOUT_GROUP_ERROR))
  }

  /**
   * 删除好友
   *
   * @param friendId
   * @return String
   */
  @ResponseBody
  @PostMapping(Array("/removeFriend"))
  def removeFriend(@RequestParam("friendId") friendId: Integer, request: HttpServletRequest): String = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val result = userService.removeFriend(friendId, user.getId)
    gson.toJson(new ResultSet(result))
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
  def changeGroup(@RequestParam("groupId") groupId: Integer, @RequestParam("userId") userId: Integer
                  , request: HttpServletRequest): String = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val result = userService.changeGroup(groupId, userId, user.getId)
    if (result) gson.toJson(new ResultSet(result))
    else gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.ERROR_MESSAGE))
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
  def refuseFriend(@RequestParam("messageBoxId") messageBoxId: Integer, request: HttpServletRequest): String = {
    val result = userService.updateAddMessage(messageBoxId, 2)
    gson.toJson(new ResultSet(result))
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
  def agreeFriend(@RequestParam("uid") uid: Integer, @RequestParam("from_group") fromGroup: Integer,
                  @RequestParam("group") group: Integer, @RequestParam("messageBoxId") messageBoxId: Integer,
                  request: HttpServletRequest): String = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val result = userService.addFriend(user.getId, group, uid, fromGroup, messageBoxId)
    if (!result) gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.ERROR_ADD_REPETITION))
    else gson.toJson(new ResultSet(result))
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
  def findAddInfo(@RequestParam("uid") uid: Int, @RequestParam("page") page: Int): String = {
    PageHelper.startPage(page, SystemConstant.ADD_MESSAGE_PAGE)
    val list = userService.findAddInfo(uid)
    val count = userService.countUnHandMessage(uid, null)
    val pages = if (count < SystemConstant.ADD_MESSAGE_PAGE) 1 else count / SystemConstant.ADD_MESSAGE_PAGE + 1
    gson.toJson(new ResultPageSet(list, pages)).replaceAll("Type", "type")
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
  def findUsers(@RequestParam(value = "page", defaultValue = "1") page: Int,
                @RequestParam(value = "name", required = false) name: String,
                @RequestParam(value = "sex", required = false) sex: Integer): String = {
    val count = userService.countUsers(name, sex)
    LOGGER.info("==> 总记录:" + count)
    val pages = if (count < SystemConstant.USER_PAGE) 1 else {
      if (count % SystemConstant.USER_PAGE == 0) count / SystemConstant.USER_PAGE
      else count / SystemConstant.USER_PAGE + 1
    }
    PageHelper.startPage(page, SystemConstant.USER_PAGE)
    val users = userService.findUsers(name, sex)
    val result = new ResultPageSet(users)
    result.setPages(pages)
    gson.toJson(result)
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
  def findGroups(@RequestParam(value = "page", defaultValue = "1") page: Int,
                 @RequestParam(value = "name", required = false) name: String): String = {
    val count = userService.countGroup(name)
    val pages = if (count < SystemConstant.USER_PAGE) 1 else {
      if (count % SystemConstant.USER_PAGE == 0) count / SystemConstant.USER_PAGE
      else count / SystemConstant.USER_PAGE + 1
    }
    PageHelper.startPage(page, SystemConstant.USER_PAGE)
    val groups = userService.findGroup(name)
    val result = new ResultPageSet(groups)
    result.setPages(pages)
    gson.toJson(result)
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
  def findMyGroups(@RequestParam(value = "page", defaultValue = "1") page: Int,
                   @RequestParam(value = "createId", required = true) createId: Integer): String = {
    val groups: util.List[GroupList] = userService.findGroupsById(createId)
    var result: ResultPageSet = null
    if (groups == null) {
      return gson.toJson(result)
    }
    val groupNews = groups.toArray.filter(x => x.asInstanceOf[GroupList].getCreateId.equals(createId))
    result = new ResultPageSet(groupNews)
    val count = groupNews.length
    val pages = if (count < SystemConstant.USER_PAGE) 1 else {
      if (count % SystemConstant.USER_PAGE == 0) count / SystemConstant.USER_PAGE
      else count / SystemConstant.USER_PAGE + 1
    }
    PageHelper.startPage(page, SystemConstant.USER_PAGE)
    result.setPages(pages)
    gson.toJson(result)
  }

  /**
   * 获取聊天记录
   *
   * @param id   与谁的聊天记录id
   * @param Type 类型，可能是friend或者是group
   * @return String
   */
  @ResponseBody
  @PostMapping(Array("/chatLog"))
  def chatLog(@RequestParam("id") id: Integer, @RequestParam("Type") Type: String,
              @RequestParam("page") page: Int, request: HttpServletRequest, model: Model): String = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    PageHelper.startPage(page, SystemConstant.SYSTEM_PAGE)
    //查找聊天记录
    val historys: util.List[ChatHistory] = userService.findHistoryMessage(user, id, Type)
    gson.toJson(new ResultSet(historys))
  }

  /**
   * 弹出聊天记录页面
   *
   * @param id   与谁的聊天记录id
   * @param Type 类型，可能是friend或者是group
   * @return String
   */
  @GetMapping(Array("/chatLogIndex"))
  def chatLogIndex(@RequestParam("id") id: Integer, @RequestParam("Type") Type: String,
                   model: Model, request: HttpServletRequest): String = {
    model.addAttribute("id", id)
    model.addAttribute("Type", Type)
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    var pages: Int = userService.countHistoryMessage(user.getId, id, Type)
    pages = if (pages < SystemConstant.SYSTEM_PAGE) pages else pages / SystemConstant.SYSTEM_PAGE + 1
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
  def getOffLineMessage(request: HttpServletRequest): String = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    LOGGER.info("查询 uid = " + user.getId + " 的离线消息")
    val receives: List[Receive] = userService.findOffLineMessage(user.getId, 0)
    receives.asScala.foreach {
      receive => {
        val user = userService.findUserById(receive.getId)
        receive.setUsername(user.getUsername)
        receive.setAvatar(user.getAvatar)
      }
    }
    gson.toJson(new ResultSet(receives)).replaceAll("Type", "type")
  }


  /**
   * 更新签名
   *
   * @param sign
   * @return String
   */
  @ResponseBody
  @PostMapping(Array("/updateSign"))
  def updateSign(request: HttpServletRequest, @RequestParam("sign") sign: String): String = {
    val user: User = request.getSession.getAttribute("user").asInstanceOf[User]
    user.setSign(sign)
    if (userService.updateSing(user)) gson.toJson(new ResultSet)
    else gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.ERROR_MESSAGE))
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
      "redirect:/#tologin?status=1" else "redirect:/#toregister?status=0"
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
  def register(@RequestBody user: User, request: HttpServletRequest): String = {
    if (userService.saveUser(user, request)) gson.toJson(new ResultSet(SystemConstant.SUCCESS, SystemConstant.REGISTER_SUCCESS))
    else gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.REGISTER_FAIL))
  }

  /**
   * 登录
   *
   * @param user
   * @return String
   */
  @ResponseBody
  @PostMapping(Array("/login"))
  def login(@RequestBody user: User, request: HttpServletRequest, response: HttpServletResponse): String = {

    val userCookie = cookieService.`match`(request)
    if (userCookie != null && user != null && userCookie.getEmail.equals(user.getEmail)
      && userCookie.getPassword.equals(user.getPassword)) {
      LOGGER.info("通过 Cookie 成功登陆服务器")
      request.getSession.setAttribute("user", userCookie)
      gson.toJson(new ResultSet(userCookie))
    } else {
      val u: User = userService.matchUser(user)
      //未激活
      if (u != null && "nonactivated".equals(u.getStatus)) {
        gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.NONACTIVED))
      } else if (u != null && !"nonactivated".equals(u.getStatus)) {
        LOGGER.info(user + "成功登陆服务器")
        request.getSession.setAttribute("user", u)
        cookieService.addCookie(u, request, response)
        gson.toJson(new ResultSet(u))
      } else {
        val result = new ResultSet(SystemConstant.ERROR, SystemConstant.LOGGIN_FAIL)
        gson.toJson(result)
      }
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
  def init(@PathVariable("userId") userId: Int): String = {
    val data = new FriendAndGroupInfo
    //用户信息
    val user = userService.findUserById(userId)
    user.setStatus("online")
    data.mine = user
    //用户群组列表
    data.group = userService.findGroupsById(userId)
    //用户好友列表
    data.friend = userService.findFriendGroupsById(userId)
    gson.toJson(new ResultSet(data))
  }

  /**
   * 获取群成员
   *
   * @param id
   * @return String
   */
  @ResponseBody
  @GetMapping(Array("/getMembers"))
  def getMembers(@RequestParam("id") id: Int): String = {
    val users = userService.findUserByGroupId(id)
    val friends = new FriendList(users)
    gson.toJson(new ResultSet(friends))
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
  def uploadImage(@RequestParam("file") file: MultipartFile, request: HttpServletRequest): String = {
    if (file.isEmpty()) gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.UPLOAD_FAIL))
    else {
      val path = request.getServletContext.getRealPath("/")
      val src = FileUtil.upload(SystemConstant.IMAGE_PATH, path, file)
      val result = new util.HashMap[String, String]
      //图片的相对路径地址
      result.put("src", src)
      LOGGER.info("图片" + file.getOriginalFilename + "上传成功")
      gson.toJson(new ResultSet(result))
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
  def uploadGroupAvatar(@RequestParam("avatar") file: MultipartFile, request: HttpServletRequest): String = {
    if (file.isEmpty()) gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.UPLOAD_FAIL))
    else {
      val path = request.getServletContext.getRealPath("/")
      val src = FileUtil.upload(SystemConstant.GROUP_AVATAR_PATH, path, file)
      val result = new util.HashMap[String, String]
      //图片的相对路径地址
      result.put("src", src)
      LOGGER.info("图片" + file.getOriginalFilename + "上传成功")
      gson.toJson(new ResultSet(result))
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
  def createGroup(@RequestBody groupList: GroupList): String = {
    val ret = userService.createGroup(groupList)
    if (ret == -1) {
      return gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.CREATE_GROUP_ERROR))
    }
    if (userService.addGroupMember(ret, groupList.getCreateId)) {
      return gson.toJson(new ResultSet(SystemConstant.SUCCESS, SystemConstant.CREATE_GROUP_SUCCCESS))
    }
    gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.CREATE_GROUP_ERROR))
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
  def uploadFile(@RequestParam("file") file: MultipartFile, request: HttpServletRequest): String = {
    if (file.isEmpty()) gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.UPLOAD_FAIL))
    else {
      val path = request.getServletContext.getRealPath("/")
      val src = FileUtil.upload(SystemConstant.FILE_PATH, path, file)
      val result = new util.HashMap[String, String]
      //文件的相对路径地址
      result.put("src", src)
      result.put("name", file.getOriginalFilename)
      LOGGER.info("文件" + file.getOriginalFilename + "上传成功")
      gson.toJson(new ResultSet(result))
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
  def updateAvatar(@RequestParam("avatar") avatar: MultipartFile, request: HttpServletRequest): String = {
    val user = request.getSession.getAttribute("user").asInstanceOf[User]
    val path = request.getServletContext.getRealPath(SystemConstant.AVATAR_PATH)
    val src = FileUtil.upload(path, avatar)
    userService.updateAvatar(user.getId, src)
    val result = new util.HashMap[String, String]
    result.put("src", src)
    gson.toJson(new ResultSet(result))
  }

  /**
   * 更新信息个人信息
   *
   * @param user
   * @return String
   */
  @ResponseBody
  @PostMapping(Array("/updateInfo"))
  def updateAvatar(@RequestBody user: UserVo, request: HttpServletRequest): String = {
    if (user == null) gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.UPDATE_INFO_FAIL))
    else {
      val u = userService.findUserById(user.getId)
      //前台明文传输，有安全问题
      if (!SecurityUtil.matchs(user.getOldpwd, u.getPassword)) {
        gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.UPDATE_INFO_PASSWORD_FAIL))
      } else {
        u.setPassword(SecurityUtil.encrypt(user.getPassword))
        val sex = if (user.getSex.equals("nan")) 0 else 1
        u.setSex(sex)
        u.setSign(user.getSign)
        u.setUsername(user.getUsername)
        userService.updateUserInfo(u, u.getId)
        gson.toJson(new ResultSet(SystemConstant.SUCCESS, SystemConstant.UPDATE_INFO_SUCCESS))
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
    LOGGER.info("用户" + user + "登陆服务器")
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
  def findUserById(@RequestParam("id") id: Integer): String = {
    gson.toJson(new ResultSet(userService.findUserById(id)))
  }

  /**
   * 判断邮件是否存在
   *
   * @param email
   * @return String
   */
  @ResponseBody
  @PostMapping(Array("/existEmail"))
  def existEmail(@RequestParam("email") email: String): String = {
    gson.toJson(new ResultSet(userService.existEmail(email)))
  }

}