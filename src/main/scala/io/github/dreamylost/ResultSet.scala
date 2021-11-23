package io.github.dreamylost

import io.github.dreamylost.constant.SystemConstant

/**
  * 结果集
  *
 * @param code 状态，0表示成功，其他表示失败
  * @param msg 额外信息
  * @since 2021年11月21日
  * @author 梦境迷离
  */
class ResultSet(
    val data: Any,
    val code: Int = SystemConstant.SUCCESS,
    val msg: String = SystemConstant.SUCCESS_MESSAGE
)

object ResultSet {

  def apply(
      data: Any = null,
      code: Int = SystemConstant.SUCCESS,
      msg: String = SystemConstant.SUCCESS_MESSAGE
  ): ResultSet = new ResultSet(data, code, msg)
}

case class ResultPageSet(override val data: Any, pages: Int) extends ResultSet(data)
