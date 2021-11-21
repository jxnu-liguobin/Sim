package io.github.dreamylost.model

import io.github.dreamylost.constant.SystemConstant

/**
  * 结果集
  *
 * @param c 状态，0表示成功，其他表示失败
  * @param m 额外信息
  * @since 2021年11月21日
  * @author 梦境迷离
  */
class ResultSet(
    val data: Any,
    val c: Int = SystemConstant.SUCCESS,
    val m: String = SystemConstant.SUCCESS_MESSAGE
)

object ResultSet {
  def apply(
      data: Any = null,
      c: Int = SystemConstant.SUCCESS,
      m: String = SystemConstant.SUCCESS_MESSAGE
  ): ResultSet = new ResultSet(data, c, m)
}

case class ResultPageSet(override val data: Any, pages: Int) extends ResultSet(data)
