package io.github.dreamylost.model

import org.bitlap.tools.JavaCompatible
import io.github.dreamylost.model.domains.FriendList
import io.github.dreamylost.model.entities.GroupList
import io.github.dreamylost.model.entities.User

/** 好友和群组整个信息集
  *
  * @param mine   我的信息
  * @param friend 好友列表
  * @param group  群组信息列表
  */
@JavaCompatible case class FriendAndGroupInfo(
    mine: User,
    friend: List[FriendList],
    group: List[GroupList]
)
