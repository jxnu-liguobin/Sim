package cn.edu.layim.domain

import cn.edu.layim.common.SystemConstant

import scala.beans.BeanProperty

/**
  * 返回值信息
  *
  * @param c 状态，0表示成功，其他表示失败
  * @param m 额外信息
  */
class ResultSet[T](c: Int = SystemConstant.SUCCESS, m: String = SystemConstant.SUCCESS_MESSAGE) {

    @BeanProperty
    var code = c

    @BeanProperty
    var msg = m

    @BeanProperty
    var data: T = _

    def this() = {
        this(SystemConstant.SUCCESS, SystemConstant.SUCCESS_MESSAGE)
    }

    def this(data: T) = {
        this
        this.data = data
    }

}