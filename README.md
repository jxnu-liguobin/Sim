[![Scala CI with Gradle](https://github.com/jxnu-liguobin/Sim/actions/workflows/gradle.yml/badge.svg)](https://github.com/jxnu-liguobin/Sim/actions/workflows/gradle.yml)

[在线测试地址 http://im.dreamylost.cn 该预览已经是zio实现的zim](http://im.dreamylost.cn:8989)

[在线API文档 swagger-ui](http://im.dreamylost.cn:8989/swagger-ui.html)

敬请关注进阶版：https://github.com/bitlap/zim 基于scala、zio、tapir、circe、akka-http、scalikejdbc、redis实现的纯异步、函数式、流式API的LayIM。


### 技术栈

- 开发语言：Scala
- 平台：JVM
- 前端：LayIM 3.0
- MVC：Spring Boot
- 数据库：Redis、MySQL
- DAO：Mybatis
- 分页：PageHelper
- 连接池：Druid
- WebSocket：Akka Actor、Akka HTTP
- 邮件：Java Mail
- API文档：Swagger
- 构建工具：Gradle
- 代码生成：scala-macro-tools

### 项目结构

```
Sim
├─ gradle                                                 - 本项目gradle使用6.5.1，高版本有bug，编译不了
├─ .gitattributes                                         - Git仓库显示语言的配置
├─ .gitignore                                             - Git忽略文件的配置
├─ .scalafmt.conf                                         - Scala代码格式化的配置
├─ README.md
├─ build.gradle
├─ deploy.sh                                              - 部署脚本
├─ settings.gradle
└─ src
       ├─ main
       │    ├─ resources
       │    │    ├─ application.conf                      - Websocket配置：基于Akka实现
       │    │    ├─ application.properties                - Springboot程序配置
       │    │    ├─ data.sql                              - 初始化数据
       │    │    ├─ favicon.ico
       │    │    ├─ layim.png
       │    │    ├─ mapper                                - mybatis mapper文件
       │    │    └─ schema.sql                            - 初始化表结构
       │    ├─ scala
       │    │    └─ io.github.dreamylost                  - 代码实现   
       │    │    └─ io.github.dreamylost.websocket        - websocket代码实现
       │    └─ webapp
       │           ├─ WEB-INF                             - 聊天记录和背景页
       │           ├─ index.html                          - 首页  
       │           └─ static                              - 静态资源文件
       └─ test
```

### 本地调试 

配置Mysql数据库，Redis以及邮件服务器，如果不需要邮件相关服务，可以在UserService.scala中注释掉相关的代码

1. 创建MySQL库 `websocket`
2. 将`schema.sql` 和 `data.sql`文件从`resources/sql/`目录移动到`resources/`，初始化表结构和数据，如需要自己mock数据，参考 `RandomData.scala` 构造
3. 查看`application.conf`和`application.properties`配置
4. 启动 `Application.scala`
5. 访问 `http://localhost:8080`
6. 登录 

选取t_user表中的任意一条数据，如：
- 邮箱 `15906184943@sina.com`
- 密码 `123456`（所有mock数据都是一个密码）
- 激活 将`status`状态改为`nonactivated`（需要激活才能登录，要配置JavaMail）

> 注意：
1. 使用环境参数`spring.profiles.active=dev`，拷贝一份配置命名为`application-dev.properties`，修改数据库信息
2. 修改model类后需要清理build目录的class文件
3. 数据库每次启动自动格式化，不需要格式化就删掉resources下的`schema.sql`和`data.sql`

> 觉得OK点个赞即可，有问题可以创建issue。

### 部署

```shell
# 在Sim目录执行
bash deploy.sh 1.3.0 # 1.3.0表示最新版本号
```

## TODO zim将完成下面工作

- [x] 使用`scalikejdbc`替代`mybatis`
- [x] 使用纯`HTML`替代余下的`jsp`
- [x] 使用`akka-http`替代`springmvc`
- [x] 使用`zio`替代`springboot`
- [ ] 使用`zio-ftp`存储图片和文件，移除`scala.util.Using`
- [x] 使用`circe`替代`Jackson`
- [x] 使用`zio-crypto`替代`spring-security`
- [x] 使用`zio-redis`替代`jedis`
- [x] 使用`sbt`替代`gradle`
- [x] 考虑使用`zio-logging`、`zio-actors`
- 达成目标：完全基于ZIO生态的安全、异步、函数式风格的Scala IM系统。（仅用于学习）
