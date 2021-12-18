package io.github.dreamylost.service

import org.bitlap.tools.log
import org.bitlap.tools.logs.LogType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

import java.io.File
import javax.mail.MessagingException

/** 邮件发送相关服务
  *
  * @since 2018年9月9日
  * @author 梦境迷离
  */
@Service
@log(logType = LogType.Slf4j)
class MailService @Autowired() (sender: JavaMailSender) {

  @Value("${spring.mail.username}")
  private var username: String = _

  /** 发送纯文本的简单邮件
    *
    * @param to      邮件接收者
    * @param subject 主题
    * @param content 内容
    */
  def sendSimpleMail(to: String, subject: String, content: String): Unit = {
    val message = new SimpleMailMessage
    message.setFrom(username)
    message.setTo(to)
    message.setSubject(subject)
    message.setText(content)
    try {
      sender.send(message)
      log.info("发送给  " + to + " 邮件发送成功")
    } catch {
      case ex: Exception =>
        log.info("发送给 " + to + " 邮件发送失败！" + ex.getMessage)
    }
  }

  /** 发送html格式的邮件
    *
    * @param to      邮件接收者
    * @param subject 主题
    * @param content 内容
    */
  def sendHtmlMail(to: String, subject: String, content: String): Unit = {
    val message = sender.createMimeMessage()
    val helper = new MimeMessageHelper(message, true)
    helper.setFrom(username)
    helper.setTo(to)
    helper.setSubject(subject)
    helper.setText(content, true)
    try {
      sender.send(message)
      log.info("发送给  " + to + " html格式的邮件发送成功")
    } catch {
      case ex: MessagingException =>
        log.info("发送给  " + to + " html格式的邮件发送失败！" + ex.getMessage)
    }
  }

  /** 发送带附件的邮件
    *
    * @param to       邮件接收者
    * @param subject  主题
    * @param content  内容
    * @param filePath 附件路径
    */
  def sendAttachmentsMail(to: String, subject: String, content: String, filePath: String): Unit = {
    val message = sender.createMimeMessage()
    val helper = new MimeMessageHelper(message, true)
    helper.setFrom(username)
    helper.setTo(to)
    helper.setSubject(subject)
    helper.setText(content, true)
    val file = new FileSystemResource(new File(filePath))
    val fileName = filePath.substring(filePath.lastIndexOf(File.separator))
    helper.addAttachment(fileName, file)
    try {
      sender.send(message)
      log.info("发送给  " + to + " 带附件邮件发送成功")
    } catch {
      case ex: MessagingException =>
        log.info("发送给   " + to + " 带附件邮件发送失败！" + ex.getMessage)
    }
  }

  /** 发送嵌入静态资源（一般是图片）的邮件
    *
    * @param to      邮件接收者
    * @param subject 主题
    * @param content 邮件内容，需要包括一个静态资源的id，比如：<img src=\"cid:rscId01\" >
    * @param rscPath 静态资源路径和文件名
    * @param rscId   静态资源id
    */
  def sendInlineResourceMail(
      to: String,
      subject: String,
      content: String,
      rscPath: String,
      rscId: String
  ): Unit = {
    val message = sender.createMimeMessage()
    val helper = new MimeMessageHelper(message, true)
    helper.setFrom(username)
    helper.setTo(to)
    helper.setSubject(subject)
    helper.setText(content, true)
    val res = new FileSystemResource(new File(rscPath))
    helper.addInline(rscId, res)
    try {
      sender.send(message)
      log.info("发送给  " + to + " 嵌入静态资源的邮件发送成功")
    } catch {
      case ex: MessagingException =>
        log.info("发送给  " + to + " 嵌入静态资源的邮件发送失败！" + ex.getMessage)
    }
  }
}
