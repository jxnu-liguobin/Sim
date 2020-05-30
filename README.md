### 主要技术 

* Scala
* Akka
* Spring Boot
* Redis
* Alibaba Druid
* Java Mail
* WebSocket
* Mybatis And PageHelper
* Swagger
* LayIM

### 环境 

* Scala 2.12.x
* JDK 1.8
* Gradle
* Mysql
* Redis 

### 使用 

配置Mysql数据库，Redis以及邮件服务器，如果不需要邮件相关服务，可以在UserService.scala中注释掉相关的代码

1. 创建MySQL库 `websocket`
2. `schema.sql` 和 `data.sql` 自动初始化表结构和数据，如需要自己mock数据，参考 `RandomData.scala` 构造
3. 查看 `application.conf` 默认可不修改
4. 启动 `Application.scala`
5. 访问 `http://localhost`
6. 登录 
```
选取t_user表中的任意一条数据，如：
邮箱 15906184943@sina.com
密码 123456（所有mock数据都是一个密码）
激活 将status状态改为 nonactivated（需要激活才能登录，要配置JavaMail）

随机的5个测试账号 密码
15803194907@yeah.net 123456
13501161119@263.net 123456
15104496675@3721.net 123456
13603931551@gmail.com 123456
15507700151@hotmail.com 123456
```

### 部署

预览 http://im.dreamylost.cn

邮箱：13706055022@googlemail.com
密码：123456

1. cd LayIM
2. gradle bootRepackage
3. java -jar dist/LayIM-1.2.1.jar

> 默认每次启动Application会自动刷新数据库，需要保留记录，请为`schema.sql`和`data.sql`重命名

### 示例

![基于Akka HTTP的LayIM](https://github.com/jxnu-liguobin/LayIM/blob/v1.2/src/main/resources/layim.png)

### v1.2.1

* 增加scalafmt格式化
* 打包并发布
* 修复建群后不刷新看不到群
* 修复同一个人的多个加群的消息被覆盖

### v1.2 版本

* 简单使用Base64编码支持cookie
* 实时输出在线用户数量
* 使用Akka HTTP重构WebSocket通信
* 升级Scala版本至2.12.8

### TODO

1. 使用Playframework、play-ws重构LayIM后端
2. 使用Akka HTTP重构LayIM后端
3. 升级LayIM到3.x

### V1.1 版本

* 查询我创建的群接口 
* 退群接口完善 
* 创建群组接口 
* 更新个人信息接口 
* 加入群组接口 
* 删除群组接口 
* 修复分页查询bug 
* 修复Redis缓存bug 
* 管理群组接口 【重命名群、修改群信息】 
* 管理好友列表接口【重命名、删除、新增】
* 若干前端问题或bug
* 代码优化

创建新的群，默认将创建者加入群中。退群的操作可能有以下情况：

1. 不允许创建者退群。  √
2. 允许创建者退群，但在退出时默认删除群。需要重置回话并发送所有提醒给群内人。感觉不太好。
3. 创建者退群时，将群中创建者【群主】，更改为最早加入者，不影响会话，只需要发送系统提示【群主已变更】。
4. 群组列表的删除和增加只能通过刷新才能显示最新数据

参考[scalad](https://github.com/scalad/LayIM)，并二次开发，是为1.1版本，还存在许多bug！！

原项目命令式风格很重，改动很多，本次没有进行重构。

包括但不限增加、修改、删除、完善代码等