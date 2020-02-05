# mall

跟着[文档](http://www.macrozheng.com) 走一遭，原项目 https://github.com/macrozheng/mall

Git 上发现一个很优秀的[项目](https://github.com/macrozheng/mall)，跟着[文档](http://www.macrozheng.com)将这个项目走一遍，学习一下新技术。创建repo的时候最好把 group 写作 macro，artifactid 写 mall-tiny 省去很多重命名的麻烦

一些缩写：

mbg: mybatis generator, 一款 mybatis 插件，自动生成 model，dao 和 mapper 的工具

工作平台
|  OS   | 版本  |
|  ----  | ----  |
| MacOS | 10.15 Catalina|
| Windows | ？|

一些思考：图片存在文件系统里面 FTS 什么的

## mall整合SpringBoot+MyBatis搭建基本骨架

### DB setup MacOS

如果以前安装过 mysql 想要重装，你需要先删除本地的一些配置文件，不然登陆可能失败

```cnf
sudo rm /usr/local/mysql
sudo rm -rf /usr/local/mysql*
sudo rm -rf /Library/StartupItems/MySQLCOM
sudo rm -rf /Library/PreferencePanes/My*
sudo rm -rf /Library/Receipts/mysql*
sudo rm -rf /Library/Receipts/MySQL*
```

1. command `brew list` and `brew services list` to check if already installed
1. `brew intall mysql` to install, after this mysql is auto up

```cnf
 jack@mac  /usr/local/var  brew install mysql
==> Downloading https://mirrors.ustc.edu.cn/homebrew-bottles/bottles/mysql-8.0.19.catalina.bottle.tar.gz
Already downloaded: /Users/jack/Library/Caches/Homebrew/downloads/5a7896f1f6b05270a9c03169efd55e4280aca8523c266c4d0d9dfca550b9342c--mysql-8.0.19.catalina.bottle.tar.gz
==> Pouring mysql-8.0.19.catalina.bottle.tar.gz
==> /usr/local/Cellar/mysql/8.0.19/bin/mysqld --initialize-insecure --user=jack --basedir=/usr/local/Cellar/mysql/8.0.19 --datadir=/usr/local/var/mysql --tmpdir=/tmp
==> Caveats
We've installed your MySQL database without a root password. To secure it run:
    mysql_secure_installation

MySQL is configured to only allow connections from localhost by default

To connect run:
    mysql -uroot

To have launchd start mysql now and restart at login:
  brew services start mysql
Or, if you don't want/need a background service you can just run:
  mysql.server start
==> Summary
🍺  /usr/local/Cellar/mysql/8.0.19: 294 files, 291.9MB
```

从安装日志来看，在安装 db 时，默认密码时空，如果你之前没有清除配置的话，可能后面就登不上了，这里是个大坑！！  
看日志很重要

1. 如果没有使用 `brew servies start mysql` 启动服务
1. run script `mysql_secure_installation` to setup password and security etc, 这个命令只有在 service 起的情况下才能被运行
1. `mysql -u root -p` to login (secure 要求至少8位密码，我用1-8)
1. 倒入表
    * 进入命令行模式
    * 创建库 mall 来存放表 `create database mall; use mall;`
    * `source mall.sql` 倒入表，确保当前路径下有这个文件
    * 也可以用 `mysql -uroot -p mall < mall.sql` 在没进去 mysql 的情况下导入

* `show databases`
* `mysqldump -uroot -p --databases test > test.sql` 导出数据
* `drop database <数据库名>;` 删除数据库
* `mysql -uroot -p < test.sql` 导入数据
* `mysql.server stop/start` or `brew services start/stop mysql` to manage mysql, brew command is prefered
* mysql_secure_installation no longer attempts to read a password from the .mysql_secret file. This was was created by mysql_install_db, a program that has been removed. (Bug #28235716, Bug #91270) 版本 8.0.16 移除密码文件

DB 链接工具我使用的是 DBeaver, 多种 DB 链接工具，免费。

### 整合 SpringBoot 和 Mybatis

由于使用的是社区版的 IDEA，我使用的是[在线 Initializr](https://start.spring.io/) 生成 web 框架

后面部分应该是 mybatis 的常规操作，但是我还是不清楚，得补完一下，先照着敲一边把

MyBatis 基本概念：

1. Mapper 配置：使用 xml 或者注解实现 Mybatis 提供的 API
1. Mapper 接口：自定义数据操作接口，自动生成mapper动态代理对象。于配置文件中的 CURD 等节点对应
1. Executor：mapper 语句执行器，核心接口
1. SQLSession： 类似JDCBC中的Connection
1. SQLSessionFactory: 拿到 session 的工厂对象

工作流：加载配置文件 -> 创建工场回话 -> 创建回话 -> 创建执行器 -> 封装SQL对象 -> 操作数据库 -> 结束

我还在教程里找了半天都没有发现 model 的定义在哪里，想了想，这些应该是从 db 的表定义里面抽取出来了，和我以前接触的流程不一样，以前都是 model 映射到 db 里面去，这里变成 db 映射到 model 了，我觉得应该是可以设置的

> 应该不能设置，就是从 db 到 model 的映射，从 mbg 的定义就能看出来 '自动生成 model，dao 和 mapper'

project, resource 路径设置 Windows 下是 '\'， Linux 下是 '/'

## mall整合Swagger-UI实现在线API文档

> 按照教程走，最后起server验证Swagger是否设置成功时，报错

```log
Field brandService in com.jzworkshop.mall.controller.PmsBrandController required a bean of type 'com.jzworkshop.mall.service.PmsBrandService' that could not be found.

The injection point has the following annotations:
	- @org.springframework.beans.factory.annotation.Autowired(required=true)
```

是由于Service实现中没有添加注解导致，在 impl 类上添加 `@Service` 问题解决

> 修完注入问题再运行，还是报错，看着像是什么配置没做好

```log
Caused by: org.apache.ibatis.builder.BuilderException: Error parsing Mapper XML. The XML location is 'file [/Users/i306454/gitStore/mall/target/classes/com/jzworkshop/mall/mbg/mapper/PmsBrandMapper.xml]'. Cause: java.lang.IllegalArgumentException: Result Maps collection already contains value for com.jzworkshop.mall.mbg.mapper.PmsBrandMapper.BaseResultMap
	at org.apache.ibatis.builder.xml.XMLMapperBuilder.configurationElement(XMLMapperBuilder.java:120) ~[mybatis-3.4.6.jar:3.4.6]
	at org.apache.ibatis.builder.xml.XMLMapperBuilder.parse(XMLMapperBuilder.java:92) ~[mybatis-3.4.6.jar:3.4.6]
	at org.mybatis.spring.SqlSessionFactoryBean.buildSqlSessionFactory(SqlSessionFactoryBean.java:521) ~[mybatis-spring-1.3.2.jar:1.3.2]
	... 70 common frames omitted
Caused by: java.lang.IllegalArgumentException: Result Maps collection already contains value for com.jzworkshop.mall.mbg.mapper.PmsBrandMapper.BaseResultMap
	at org.apache.ibatis.session.Configuration$StrictMap.put(Configuration.java:872) ~[mybatis-3.4.6.jar:3.4.6]
	at org.apache.ibatis.session.Configuration$StrictMap.put(Configuration.java:844) ~[mybatis-3.4.6.jar:3.4.6]
	at org.apache.ibatis.session.Configuration.addResultMap(Configuration.java:626) ~[mybatis-3.4.6.jar:3.4.6]
	at org.apache.ibatis.builder.MapperBuilderAssistant.addResultMap(MapperBuilderAssistant.java:214) ~[mybatis-3.4.6.jar:3.4.6]
	at org.apache.ibatis.builder.ResultMapResolver.resolve(ResultMapResolver.java:47) ~[mybatis-3.4.6.jar:3.4.6]
	at org.apache.ibatis.builder.xml.XMLMapperBuilder.resultMapElement(XMLMapperBuilder.java:285) ~[mybatis-3.4.6.jar:3.4.6]
	at org.apache.ibatis.builder.xml.XMLMapperBuilder.resultMapElement(XMLMapperBuilder.java:252) ~[mybatis-3.4.6.jar:3.4.6]
	at org.apache.ibatis.builder.xml.XMLMapperBuilder.resultMapElements(XMLMapperBuilder.java:244) ~[mybatis-3.4.6.jar:3.4.6]
	at org.apache.ibatis.builder.xml.XMLMapperBuilder.configurationElement(XMLMapperBuilder.java:116) ~[mybatis-3.4.6.jar:3.4.6]
	... 72 common frames omitted
```

搜了下，很多其他人也遇到过这个问题，是应为在在 mbg 的时候没有删掉原来 resources 下的 PmsBrandMapper.xml 文件，导致重合了。删掉重新运行 generator 生成即可。

> 服务器起了，也面也能出来，但是文档没有 controller 相关的信息

这是由于我包名和作者的不一样，在配置 Swagger2Config 文件的时候没有注意。修改后成功生成文档, 功能正常，nice。

## Redis的安装和启动

brew 有 redis 安装包，可以直接通过 `brew install redis` 安装，安装之后会有操作提示

```txt
To have launchd start redis now and restart at login:
  brew services start redis
Or, if you don't want/need a background service you can just run:
  redis-server /usr/local/etc/redis.conf
```

不知道是系统原因还是咋地，装完之后 brew services 命令不能用了，害得我重新装了一遍 brew，卸载brew 会把之前通过它安装的软件都卸载掉。。。。还在重装之后都能work。

这一章难度不大，按照提示来，基本上很快就能做完

## mall整合SpringSecurity和JWT实现认证和授权（一）

作者没有在文档中说明 model 怎么生成的，不过如果之前的章节都充分理解了，那么很容易猜到是在 generatorConfig.xml 里面添加了对应的表，逆向生成的，在进行这个章节内容之前先运行一下 mbg 生成所需的class

新出现了一个名次 dto, 和 dao 比较接近， dao 是 data access object, 数据访问对象。dto 是 data transfer object 数据传输对象

> 启动服务器失败报错, 猜测是 JwtAuthenticationTokenFilter 里面 jwtTokenUtil 里的什么东西有问题

```log
Caused by: java.lang.IllegalArgumentException: Could not resolve placeholder 'jwt.secret' in value "${jwt.secret}"
```

作者忘了说明需要在配置文件中添加 jwt 的config, 搜素了git 才发现的

> 修完之后又报错了, 应该是我之前写code 的时候未解决的疑问，dao 只写了接口，没有写实现

```log
Description:

Field adminRoleRelationDao in com.jzworkshop.mall.service.impl.UmsAdminServiceImpl required a bean of type 'com.jzworkshop.mall.dao.UmsAdminRoleRelationDao' that could not be found.

The injection point has the following annotations:
	- @org.springframework.beans.factory.annotation.Autowired(required=true)
```

解决方案应该是在 resource 下面添加对应的实现，看格式应该是手动写的

尝试失败，还是会报错，然后又找找找，突然想到，这种东西可能还加了一些db 配置，然后去 MyBatisConfig 里面看了下果然少了一个 dao 的 map。启动成功。

值得一题的是，我看到手写的这个 mapper 里面有 `<select id="getPermissionList" resultMap="com.jzworkshop.mall.mbg.mapper.UmsPermissionMapper.BaseResultMap">` 这样的设定，反复对比了code也没有在UmsPermissionMapper 里找到这个 BaseResultMap, 最后在对应的 xml 里找到了定义

功能基本完成了，但是也就走了一把，知道了大概要配点什么，用什么工具，入过真要自己去写一个新的应用实现什么功能的话，差的还有点远，需要自己再做个什么东西实践一下才行，不过至少走完之后你可以有个数

## mall整合SpringTask实现定时任务

按着教程走就完事儿了，5分钟搞定

## mall整合Elasticsearch实现商品搜索

brew install elasticsearch kibana, 安装log

```log
To have launchd start elasticsearch now and restart at login:
  brew services start elasticsearch
Or, if you don't want/need a background service you can just run:
  elasticsearch

To have launchd start kibana now and restart at login:
  brew services start kibana
Or, if you don't want/need a background service you can just run:
  kibana
```

elasticsearch 启动失败，报错

```log
ERROR: Cluster name [elasticsearch_jack] subdirectory exists in data paths [/usr/local/var/lib/elasticsearch/elasticsearch_jack].
```

查了下好像是因为以前安装过 elasticsearch, 新版的目录结构变了，直接把老的文件夹删掉就行了，就是log 里面出现的那个。访问 http://localhost:9200/ 以验证安装

访问 http://localhost:5601/ 以验证 kibana 的安装情况

后台运行的情况下可以查看 `/usr/local/var/log/` 文件夹获取对应的 log

> 按教程走完，启动报错

```log
...
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'esProductRepository': Invocation of init method failed; nested exception is org.springframework.data.mapping.MappingException: Couldn't find PersistentEntity for type class java.lang.Object!
...
Caused by: org.springframework.data.mapping.MappingException: Couldn't find PersistentEntity for type class java.lang.Object!
```

查看代码发现 'EsProductRepository extends ElasticsearchRepository<EsProduct, Long>' 定义的时候忘记添加范型了

> 启动后，调用 API 失败， 配置有问题
> 1.端口是9300，而不是9200，9300 是 Java 客户端的端口。9200是支持 Restful HTTP的端口
> cluster-name: elasticsearch 有问题，需要和 http://127.0.0.1:9200/ 这个页面上的 name 保持一致

这些都修改完后就可以正常运行了