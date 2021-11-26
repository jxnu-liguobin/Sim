package io.github.dreamylost.model

/** 我发送的消息和我的信息
  *
  * @param id       我的id
  * @param username 我的昵称
  * @param mine     是否我发的消息
  * @param avatar   我的头像
  * @param content  消息内容
  */
case class Mine(id: Int, username: String, mine: Boolean, avatar: String, content: String)
