package cn.edu.layim.entity

import java.util.Date

import org.hibernate.validator.constraints.NotEmpty
import org.springframework.format.annotation.DateTimeFormat

import scala.beans.BeanProperty

/**
  * 用户
  *
 * @see table:t_user
  * @date 2018年9月8日
  * @author 梦境迷离
  *
 */
class User extends Serializable {

  @BeanProperty
  var id: Int = _

  //用户名
  @BeanProperty
  @NotEmpty
  var username: String = _

  //密码
  @BeanProperty
  @NotEmpty
  var password: String = _

  //签名
  @BeanProperty
  @NotEmpty
  var sign: String = _

  //头像
  @BeanProperty
  var avatar: String = _

  //邮箱
  @BeanProperty
  @NotEmpty
  var email: String = _

  //创建时间
  @BeanProperty
  //@NotNull
  @DateTimeFormat(pattern = "yyyy-MM-dd")
  var createDate: Date = _

  //性别
  @BeanProperty
  var sex: Int = _

  //状态
  @BeanProperty
  var status: String = _

  //激活码
  @BeanProperty
  var active: String = _

  override def toString =
    s"User(id=$id, username=$username, password=$password, sign=$sign, avatar=$avatar, email=$email, createDate=$createDate, sex=$sex, status=$status, active=$active)"
}
