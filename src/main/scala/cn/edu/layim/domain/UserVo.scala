package cn.edu.layim.domain

import org.hibernate.validator.constraints.NotEmpty

import scala.beans.BeanProperty

/**
  * 返回个人信息更新
  *
 * @author 梦境迷离
  * @time 2018-10-20
  */
class UserVo extends Serializable {

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

  //旧密码
  @BeanProperty
  @NotEmpty
  var oldpwd: String = _

  //签名
  @BeanProperty
  @NotEmpty
  var sign: String = _

  //性别
  @BeanProperty
  var sex: String = _

  override def toString =
    s"UserVo(id=$id, username=$username, password=$password, oldpwd=$oldpwd, sign=$sign, sex=$sex)"
}
