package cn.edu.layim.controller

import java.util.{HashMap, List}

import cn.edu.layim.common.SystemConstant
import cn.edu.layim.domain.{FriendAndGroupInfo, FriendList, ResultPageSet, ResultSet}
import cn.edu.layim.entity.{ChatHistory, Receive, User}
import cn.edu.layim.service.UserService
import cn.edu.layim.util.FileUtil
import com.github.pagehelper.PageHelper
import com.google.gson.Gson
import io.swagger.annotations.{Api, ApiOperation}
import javax.servlet.http.HttpServletRequest
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation._
import org.springframework.web.multipart.MultipartFile

import scala.collection.JavaConversions

/**
  * 用户接口
  *
  * @date 2018年9月9日
  * @author 梦境迷离
  */
@Controller
@Api(value = "用户相关操作")
@RequestMapping(value = Array("/user"))
class UserController @Autowired()(private val userService: UserService) {

    private final val LOGGER: Logger = LoggerFactory.getLogger(classOf[UserController])

    private final val gson: Gson = new Gson

    /**
      * 退出群
      *
      * @param groupId 群编号
      * @param request
      * @return
      */
    @ResponseBody
    @PostMapping(Array("/leaveOutGroup"))
    def leaveOutGroup(@RequestParam("groupId") groupId: Integer, request: HttpServletRequest): String = {
        val user = request.getSession.getAttribute("user").asInstanceOf[User]
        val result = userService.leaveOutGroup(groupId, user.getId)
        gson.toJson(new ResultSet(result))
    }

    /**
      * 删除好友
      *
      * @param friendId
      * @return
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
      * @return
      */
    @ResponseBody
    @PostMapping(Array("/changeGroup"))
    def changeGroup(@RequestParam("groupId") groupId: Integer, @RequestParam("userId") userId: Integer
                    , request: HttpServletRequest): String = {
        val user = request.getSession.getAttribute("user").asInstanceOf[User]
        val result = userService.changeGroup(groupId, userId, user.getId)
        if (result) {
            return gson.toJson(new ResultSet(result))
        } else {
            gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.ERROR_MESSAGE))
        }
    }

    /**
      * 拒绝添加好友
      *
      * @param request
      * @param messageBoxId 消息盒子的消息id
      * @return
      */
    @ResponseBody
    @PostMapping(Array("/refuseFriend"))
    def refuseFriend(@RequestParam("messageBoxId") messageBoxId: Integer, request: HttpServletRequest): String = {
        val result = userService.updateAddMessage(messageBoxId, 2)
        gson.toJson(new ResultSet(result))
    }

    /**
      * 添加好友
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
        gson.toJson(new ResultSet(result))
    }

    /**
      * 查询消息盒子信息
      *
      * @param uid
      * @param page
      */
    @ResponseBody
    @GetMapping(Array("/findAddInfo"))
    def findAddInfo(@RequestParam("uid") uid: Integer, @RequestParam("page") page: Int): String = {
        PageHelper.startPage(page, SystemConstant.ADD_MESSAGE_PAGE)
        val list = userService.findAddInfo(uid)
        val count = userService.countUnHandMessage(uid, null).toInt
        val pages = if (count < SystemConstant.ADD_MESSAGE_PAGE) 1 else count / SystemConstant.ADD_MESSAGE_PAGE + 1
        gson.toJson(new ResultPageSet(list, pages)).replaceAll("Type", "type")
    }

    /**
      * 分页查找好友
      *
      * @param page 第几页
      * @param name 好友名字
      * @param sex  性别
      */
    @ResponseBody
    @GetMapping(Array("/findUsers"))
    def findUsers(@RequestParam(value = "page", defaultValue = "1") page: Int,
                  @RequestParam(value = "name", required = false) name: String,
                  @RequestParam(value = "sex", required = false) sex: Integer): String = {
        val count = userService.countUsers(name, sex)
        val pages = if (count < SystemConstant.USER_PAGE) 1 else (count / SystemConstant.USER_PAGE + 1)
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
      */
    @ResponseBody
    @GetMapping(Array("/findGroups"))
    def findGroups(@RequestParam(value = "page", defaultValue = "1") page: Int,
                   @RequestParam(value = "name", required = false) name: String): String = {
        val count = userService.countGroup(name)
        val pages = if (count < SystemConstant.USER_PAGE) 1 else (count / SystemConstant.USER_PAGE + 1)
        PageHelper.startPage(page, SystemConstant.USER_PAGE)
        val groups = userService.findGroup(name)
        val result = new ResultPageSet(groups)
        result.setPages(pages)
        gson.toJson(result)
    }

    /**
      * 获取聊天记录
      *
      * @param id   与谁的聊天记录id
      * @param Type 类型，可能是friend或者是group
      */
    @ResponseBody
    @PostMapping(Array("/chatLog"))
    def chatLog(@RequestParam("id") id: Integer, @RequestParam("Type") Type: String,
                @RequestParam("page") page: Int, request: HttpServletRequest, model: Model): String = {
        val user = request.getSession.getAttribute("user").asInstanceOf[User]
        PageHelper.startPage(page, SystemConstant.SYSTEM_PAGE)
        //查找聊天记录
        val historys: List[ChatHistory] = userService.findHistoryMessage(user, id, Type)
        gson.toJson(new ResultSet(historys))
    }

    /**
      * 弹出聊天记录页面
      *
      * @param id   与谁的聊天记录id
      * @param Type 类型，可能是friend或者是group
      */
    @GetMapping(Array("/chatLogIndex"))
    def chatLogIndex(@RequestParam("id") id: Integer, @RequestParam("Type") Type: String,
                     model: Model, request: HttpServletRequest): String = {
        model.addAttribute("id", id)
        model.addAttribute("Type", Type)
        val user = request.getSession.getAttribute("user").asInstanceOf[User]
        var pages: Int = userService.countHistoryMessage(user.getId, id, Type)
        pages = if (pages < SystemConstant.SYSTEM_PAGE) pages else (pages / SystemConstant.SYSTEM_PAGE + 1)
        model.addAttribute("pages", pages)
        "chatLog"
    }

    /**
      * 获取离线消息
      */
    @ResponseBody
    @PostMapping(Array("/getOffLineMessage"))
    def getOffLineMessage(request: HttpServletRequest): String = {
        val user = request.getSession.getAttribute("user").asInstanceOf[User]
        LOGGER.info("查询 uid = " + user.getId + " 的离线消息")
        val receives: List[Receive] = userService.findOffLineMessage(user.getId, 0)
        JavaConversions.collectionAsScalaIterable(receives).foreach {
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
      *
      */
    @ResponseBody
    @PostMapping(Array("/updateSign"))
    def updateSign(request: HttpServletRequest, @RequestParam("sign") sign: String): String = {
        val user: User = request.getSession.getAttribute("user").asInstanceOf[User]
        user.setSign(sign)
        if (userService.updateSing(user)) {
            gson.toJson(new ResultSet)
        } else {
            gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.ERROR_MESSAGE))
        }
    }

    /**
      * 激活
      *
      * @param activeCode
      *
      */
    @GetMapping(Array("/active/{activeCode}"))
    def activeUser(@PathVariable("activeCode") activeCode: String): String = {
        if (userService.activeUser(activeCode) == 1) {
            return "redirect:/#tologin?status=1"
        }
        "redirect:/#toregister?status=0"
    }

    /**
      * 注册
      *
      * @param user
      *
      */
    @ResponseBody
    @PostMapping(Array("/register"))
    def register(@RequestBody user: User, request: HttpServletRequest): String = {
        if (userService.saveUser(user, request)) {
            gson.toJson(new ResultSet[String](SystemConstant.SUCCESS, SystemConstant.REGISTER_SUCCESS))
        } else {
            gson.toJson(new ResultSet[String](SystemConstant.ERROR, SystemConstant.REGISTER_FAIL))
        }
    }

    /**
      * 登陆
      *
      * @param user
      *
      */
    @ResponseBody
    @PostMapping(Array("/login"))
    def login(@RequestBody user: User, request: HttpServletRequest): String = {
        val u: User = userService.matchUser(user)
        //未激活
        if (u != null && "nonactivated".equals(u.getStatus)) {
            return gson.toJson(new ResultSet[User](SystemConstant.ERROR, SystemConstant.NONACTIVED))
        } else if (u != null && !"nonactivated".equals(u.getStatus)) {
            LOGGER.info(user + "成功登陆服务器")
            request.getSession.setAttribute("user", u);
            return gson.toJson(new ResultSet[User](u))
        } else {
            val result = new ResultSet[User](SystemConstant.ERROR, SystemConstant.LOGGIN_FAIL)
            gson.toJson(result)
        }
    }

    /**
      * 初始化主界面数据
      *
      * @param userId
      *
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
        gson.toJson(new ResultSet[FriendAndGroupInfo](data))
    }

    /**
      * 获取群成员
      *
      * @param id
      *
      */
    @ResponseBody
    @GetMapping(Array("/getMembers"))
    def getMembers(@RequestParam("id") id: Int): String = {
        val users = userService.findUserByGroupId(id)
        val friends = new FriendList(users)
        gson.toJson(new ResultSet[FriendList](friends))
    }

    /**
      * 客户端上传图片
      *
      * @param file
      * @param request
      * @return
      */
    @ResponseBody
    @PostMapping(Array("/upload/image"))
    def uploadImage(@RequestParam("file") file: MultipartFile, request: HttpServletRequest): String = {
        if (file.isEmpty()) {
            return gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.UPLOAD_FAIL))
        }
        val path = request.getServletContext.getRealPath("/")
        val src = FileUtil.upload(SystemConstant.IMAGE_PATH, path, file)
        val result = new HashMap[String, String]
        //图片的相对路径地址
        result.put("src", src)
        LOGGER.info("图片" + file.getOriginalFilename + "上传成功")
        gson.toJson(new ResultSet[HashMap[String, String]](result))
    }

    /**
      * 客户端上传文件
      *
      * @param file
      * @param request
      * @return
      */
    @ResponseBody
    @PostMapping(Array("/upload/file"))
    def uploadFile(@RequestParam("file") file: MultipartFile, request: HttpServletRequest): String = {
        if (file.isEmpty()) {
            return gson.toJson(new ResultSet(SystemConstant.ERROR, SystemConstant.UPLOAD_FAIL))
        }
        val path = request.getServletContext.getRealPath("/")
        val src = FileUtil.upload(SystemConstant.FILE_PATH, path, file)
        val result = new HashMap[String, String]
        //文件的相对路径地址
        result.put("src", src)
        result.put("name", file.getOriginalFilename)
        LOGGER.info("文件" + file.getOriginalFilename + "上传成功")
        gson.toJson(new ResultSet[HashMap[String, String]](result))
    }

    /**
      * 用户更新头像
      *
      * @param avatar
      */
    @ResponseBody
    @PostMapping(Array("/updateAvatar"))
    def updateAvatar(@RequestParam("avatar") avatar: MultipartFile, request: HttpServletRequest): String = {
        val user = request.getSession.getAttribute("user").asInstanceOf[User]
        val path = request.getServletContext.getRealPath(SystemConstant.AVATAR_PATH)
        val src = FileUtil.upload(path, avatar)
        userService.updateAvatar(user.getId, src)
        val result = new HashMap[String, String]
        result.put("src", src)
        gson.toJson(new ResultSet(result))
    }

    /**
      * 跳转主页
      *
      * @param model
      * @param request
      * @return
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
      * @return
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
      * @return
      */
    @ResponseBody
    @PostMapping(Array("/existEmail"))
    def existEmail(@RequestParam("email") email: String): String = {
        gson.toJson(new ResultSet(userService.existEmail(email)))
    }

}