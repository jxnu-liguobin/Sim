package cn.edu.layim.domain

import scala.beans.BeanProperty

/**
 * 分页结果集
 *
 * @date 2018年9月8日
 * @author 梦境迷离
 */
class ResultPageSet[T] extends ResultSet[T] {

  @BeanProperty
  var pages: Int = _

  def this(data: T) = {
    this
    this.data = data
  }

  def this(data: T, pages: Int) = {
    this
    this.data = data
    this.pages = pages
  }
}