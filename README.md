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

### 项目结构

```
LayIM
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
2. `schema.sql` 和 `data.sql` 初始化表结构和数据，如需要自己mock数据，参考 `RandomData.scala` 构造
3. 查看 `application.conf` WebSocket配置
4. 启动 `Application.scala`
5. 访问 `http://localhost`
6. 登录 

选取t_user表中的任意一条数据，如：
邮箱 `15906184943@sina.com`
密码 `123456`（所有mock数据都是一个密码）
激活 将`status`状态改为`nonactivated`（需要激活才能登录，要配置JavaMail）

### 部署

```shell
# 在LayIM目录执行
# 里面的jar包版本号需要改
bash deploy.sh
```
