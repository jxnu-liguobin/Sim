package cn.edu.layim.domain

import scala.beans.BeanProperty

/**
  * 组信息
  *
  * @param id        群组ID
  * @param groupName 群组名
  *
  */
class Group(@BeanProperty var id: Integer, @BeanProperty var groupName: String)