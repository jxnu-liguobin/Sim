package io.github.dreamylost.util

import io.github.dreamylost.constant.SystemConstant
import org.springframework.web.multipart.MultipartFile

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import scala.util.Using

/** 服务器文件工具
  *
  * @since 2018年9月8日
  * @author 梦境迷离
  */
object FileUtil {

  /** 文件保存服务器
    *
    * @param `type` 文件类型/upload/image 或  /upload/file
    * @param path   文件绝对路径地址
    * @param file   二进制文件
    * @return 文件的相对路径地址
    */
  def upload(`type`: String, path: String, file: MultipartFile): String = {
    var name = file.getOriginalFilename
    var paths = path + `type` + DateUtil.getDateString + "/"
    var result = `type` + DateUtil.getDateString + "/"
    //如果是图片，则使用uuid重命名图片
    if (
      SystemConstant.IMAGE_PATH.equals(`type`) || SystemConstant.GROUP_AVATAR_PATH.equals(`type`)
    ) {
      name = UUIDUtil.getUUID32String() + name.substring(name.indexOf("."))
    } else if (SystemConstant.FILE_PATH.equals(`type`)) {
      //如果是文件，则区分目录
      val p = UUIDUtil.getUUID32String()
      paths = paths + p
      result += p + "/"
    }
    copyInputStreamToFile(file.getInputStream, new File(paths, name))
    result + name
  }

  /** 用户更新头像
    *
    * @param realpath 服务器绝对路径地址
    * @param file     文件
    * @return 相对路径
    */
  def upload(realpath: String, file: MultipartFile): String = {
    var name = file.getOriginalFilename
    name = UUIDUtil.getUUID32String + name.substring(name.indexOf("."))
    copyInputStreamToFile(file.getInputStream, new File(realpath, name))
    SystemConstant.AVATAR_PATH + name
  }

  def copyInputStreamToFile(inputStream: InputStream, file: File): Unit = {
    if (file.exists) {
      if (file.isDirectory) throw new IOException(s"File $file exists but is a directory")
      if (!file.canWrite) throw new IOException(s"File $file cannot be written to")
    } else {
      val parent = file.getParentFile
      if (parent != null) if (!parent.mkdirs && !parent.isDirectory) {
        throw new IOException(s"Directory $parent could not be created")
      }
    }
    Using.resources(inputStream, new FileOutputStream(file))((in, out) => {
      val data = Iterator.continually(in.read()).takeWhile(_ != -1).map(_.toByte).toArray
      out.write(data)
    })
  }

}
