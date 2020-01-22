package cn.edu.layim.entity

import scala.beans.BeanProperty

/**
 * 群组
 *
 * @param id        群组id
 * @param groupname 群组名
 * @date 2018年9月8日
 * @author 梦境迷离
 */
class Group(@BeanProperty var id: Integer, @BeanProperty var groupname: String)