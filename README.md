## SpringBoot-LayIM-Scala ###

### 主要技术 ### 

    LayIM
    WebSocket
    Gradle
    Spring Boot
    Scala
    Redis
    Alibaba Druid
    Java Mail
    Mybatis And PageHelper
    Swagger

### 环境 ###

* Scala 2.11.8
* JDK 1.8
* Gradle 3.0
* Mysql
* Redis 

### 使用 ###

配置Mysql数据库，Redis以及邮件服务器，如果不需要邮件相关服务，可以在UserService.scala中注释掉相关的代码

启动：Application.scala

访问：http://localhost

完善接口：

1. 查询我创建的群接口 √
2. 退群接口完善 √
3. 创建群组接口 √
4. 管理群组接口 【增删改】
5. 管理好友列表接口【增删改】
6. 更新个人信息接口 √
7. 屏蔽好友接口
8. 加入群组接口 √
9. 删除群组接口 √
10. 修复分页查询bug √
11. 修复Redis缓存bug √


创建新的群，默认将创建者加入群中。退群的操作可能有以下情况：

1. 不允许创建者退群。  √
2. 允许创建者退群，但在退出时默认删除群。需要重置回话并发送所有提醒给群内人。感觉不太好。
3. 创建者退群时，将群中创建者【群主】，更改为最早加入者，不影响会话，只需要发送系统提示【群主已变更】。

需要注意的是：

二次开发，部分变量命名不规范，但是也不方便去修改 。 
群组列表的删除和增加只能通过刷新才能显示最新数据

参考[scalad](https://github.com/scalad/LayIM)，并二次开发

包括但不限增加、修改、删除、完善代码等

