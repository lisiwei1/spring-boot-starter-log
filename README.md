# spring-boot-starter-log

## 简介

此项目为日志框架的starter的源码，直接下载后打包到maven，然后其他工程通过maven导入即可（下面会详细描述使用方法）。

使用AOP拦截web请求和@Scheduled的定时任务，记录下相关的参数的日志，可以将此日志导入ELK实现秒查日志，并且支持链路追踪。



默认日志格式如下，如有需要可以自行修改源码。如果有bug或者新需求请提issue。

```
{
  "classMethod": "api",
  "hostName": "LAPTOP-572VT37S",
  "consumeTime": 57,
  "hostIp": "192.168.0.118",
  "responseTime": "2023-05-06 13:39:35.840",
  "requestParams": "{\"code\":\"22\"}",
  "serverName": "admin-server-01",
  "requestIp": "127.0.0.1",
  "className": "com.logdemo.test.web.TestController",
  "httpMethod": "GET",
  "url": "http://127.0.0.1:20230/test/api",
  "sqls": [],
  "requestTime": "2023-05-06 13:39:35.783",
  "responseParams": "调用成功！",
  "traceToken": "e9ff6423-d14c-470e-be32-3f953f89b2f3",
  "currentOrder": 1,
  "desc": "测试API接口"
}
```



## 效果演示

控制台输出：

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/7dd73883-d00a-4cf0-8cdb-ec901ef4b4e8)


ELK界面（下面会讲如何进行ELK配置和简单使用）

![image](https://user-images.githubusercontent.com/44285123/236731300-f7cc672d-9eac-43dd-a53a-7b38982fc690.png)





## 使用说明



### 导入starter

第一步，下载源码，用idea 打开，然后打开maven执行package，执行成功后在执行install，这样就把代码打包到本地maven仓库。

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/4af78418-78e0-43c4-9714-9da9e86fa0b5)


第二部，打开需要用到此starter的工程，打开pom导入starter，然后刷新maven。

```
<dependency>
    <groupId>com.lsw.log</groupId>
    <artifactId>spring-boot-starter-log</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/fa73ee5f-fa3a-462b-a0ef-00114ed1e2db)
![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/8da96f9c-5134-44cc-806f-8cebfcfff9d0)


上面都完成后，执行一个web请求即可看到控制台的日志输出。（建议先新建一个demo来测试）

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/9e972a0d-ce71-4a01-96c9-5b05924b772f)




### 配置日志中的serverName字段

日志中的serverName字段是读配置文件的serverName（默认 default-server-01），是用来定义服务节点名称的，建议最好配一下，每一个服务都不一样，方便后面查日志时区分是哪一个服务记录的日志。而配置文件中的excludeClassNames（默认空）是用于指定特定下的包跳过日志记录的，多个包名用英文逗号隔开。

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/40303216-aa83-4552-bad2-01b0f2c84ac2)


### 跳过日志记录

有些接口的日志比较大不想记录，那么可以按照下面的方法进行配置跳过日志记录



#### 1、注解方式

如果一些请求入参或者出参过大，不想记录，或者指定一些方法都不记录日志，直接在对应方法上添加@LogOperation注解来实现，比如@LogOperation(value = "测试API接口", skipReq= true)这个注解就记录当前注解所在的方法的方法描述为 "测试API接口"，对应日志的desc字段，而skipReq= true表示此方法跳过日志入参，skipLog= true则是不记录日志。

```
/**
 * 日志注解
 * @Author lsw
 * @Date 2023/5/6 13:04
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    String value() default "";//接口名字

    boolean skipLog() default false;//不记录整体日志

    boolean skipReq() default false;//不记录请求日志

    boolean skipRsp() default false;//不记录响应日志

    boolean skipSql() default false;//跳过sql

}
```

#### 2、配置文件方式

指定特定包名下的类下的方法不记录日志。

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/2be460f3-d943-459e-9179-752d66386aa6)
注意是填package名，不要包含类名。（未实现指定类的功能 ，后面有时间再加上此功能。）

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/d49ca2f6-e481-4c3c-a6e6-966d05b57c66)


#### 3、直接改starter源码

只记录某些注解下的日志或者不记录某些注解下的，比如指记录Get方法的日志，或者只记录@Scheduled下定时任务的日志

那直接修改代码，找到com.log.core.log包下的LogAspect文件进行修改。

比如不记录标有@GetMapping下的方法，那就删除对应切点（下图红框处）

![image](https://user-images.githubusercontent.com/44285123/236731678-b042a139-77ad-4349-a375-2143a6910f8f.png)



### 获取方法名称/说明

获取到方法说明写入日志的desc字段

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/021a9029-b276-43db-ab69-8d693f6f2f43)
![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/7a6ff87e-d5ff-45ae-a190-bfa85ff29ca7)


新建一个配置文件，实现MethodDescConfigurer接口，重写getMethodDesc方法，自己写获取指定注解的数据的逻辑

比如我现在的项目是使用我自己写的自定义注解@MethodDesc的value值来记录方法的说明的，那就按照下图的代码逻辑来获取方法说明。其他注解，比如swagger的@ApiOperation也是用相同逻辑获取注解里面的信息。

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/ac43f807-7b9a-4342-9832-72ae4a7f0d82)




### logback.xml配置文件

如果当前项目没有logback.xml，请复制starter中的logback.xml到本地，这样才能在控制台输出日志的同时将日志记录到本地日志文件，并且使用ELK时也要用到logback.xml。（logback.xml文件在 【日志配置文件】的文件夹中）



## ELK配置与使用

如果原工程已有logback.xml文件，则直接复制下面的配置到日志配置文件

```
<!--输出到logstash的appender，需要用到ELK请取消注释-->
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
<!--可以访问的logstash日志收集端口-->
<destination>127.0.0.1:4560</destination>
<encoder charset="UTF-8" class="net.logstash.logback.encoder.LogstashEncoder">
</encoder>
</appender>

<logger name="LogPackage" level="INFO">
<!--需要用到ELK请取消注释-->
<appender-ref ref="LOGSTASH" />
</logger>
```



如果原工程没用日志配置文件logback.xml，请直接复制ogback.xml文件在resources目录下，并取消下图中的注释配置。（logback.xml文件在 【日志配置文件】的文件夹中）

![image](https://github.com/lisiwei1/spring-boot-starter-log/assets/44285123/a860b918-b7ca-4ef9-a7ea-a10bf857f2ef)




自行安装ELK（elasticsearch、Logstash和kibana），此处使用的是7.15版本，而且项目和ELK都是本机环境下运行的，如果不是请自行修改对应配置的地址和端口。

![image](https://user-images.githubusercontent.com/44285123/236731401-d07ed208-ae0d-40e2-a878-eb6489b71890.png)



#### ELK文件配置

在logstash的bin目录下新增logstash.conf文件

![image](https://user-images.githubusercontent.com/44285123/236731416-259681b5-bf6b-43d5-8524-2e4150de95d8.png)

往文件里面添加下面内容。其中input-tcp下的host和port要跟logbacj.xml中的destination配置一致。

然后修改output-elasticsearch-index的内容，即"weblog-info-test-%{+YYYY.MM.dd}"，自定义索引名称请改weblog-info-test这部分，此处是设置日志索引名称的，后面创建索引模式需要用到，到时需要用到 weblog-info-test-*

```
input {
  tcp {
    mode => "server"
    host => "0.0.0.0"
    port => 4560
    codec => json_lines
  }
}

filter {
  json {
    # 不加这段会只有message字段，值都到message里面，没有各个字段的索引
    source => "message"
    remove_field => ["message"]
  }
}

output {
  elasticsearch {
    hosts => "127.0.0.1:9200"
    index => "weblog-info-test-%{+YYYY.MM.dd}"
  }
}
```



#### ELK启动

```
1.运行elasticsearch
双击执行bin目录下的elasticsearch.bat
启动后可以打开http://localhost:9200查看是否启动成功

2.运行Logstash
切换到bin目录，在地址栏输入cmd回车后，执行logstash -f logstash.conf

3.运行kibana
执行bin目录下的kibana.bat
然后打开http://localhost:5601/，此界面就是ELK的使用界面
```



#### ELK使用

ELK是可以设置密码的，有需要请自行设置

启动ELK后，用浏览器打开http://localhost:5601/

点击左侧的stack management

![image](https://user-images.githubusercontent.com/44285123/236731440-1a114e80-d849-45e5-9eb2-fb6f7022f16d.png)

然后点击索引模式，再点击右上角的【创建索引模式】

![image](https://user-images.githubusercontent.com/44285123/236731575-e18ef1cb-6c42-4f6a-a2e4-ad25e557cefb.png)

然后创建索引模式

![image](https://user-images.githubusercontent.com/44285123/236731588-f673da1a-2257-4bc9-a11a-8b7aa0b20c56.png)

然后打开Discover选择刚刚创建的索引模式

![image](https://user-images.githubusercontent.com/44285123/236731601-0b0ef38b-2d5a-4889-ad98-e4d63d430792.png)

![image](https://user-images.githubusercontent.com/44285123/236731610-7b274427-c2b9-4424-b35e-e2e68629a8e3.png)

然后就可以在ELK上愉快的查询日志了，可以指定时间，指定日志里面的字段进行查询，而且是秒查。

![image](https://user-images.githubusercontent.com/44285123/236731614-64903c17-12e3-4cbc-8766-cef4344cfed1.png)



## 链路追踪
链路追踪是指一个web请求发送到服务A，然后服务A通过HTTP调用服务B的的web服务，这种情况下，服务A和服务B记录到的日志的tracetoken是一样的。那就可以使用日志中的tracetoken字段查出这两个服务之间调用时记录的日志。

使用tracetoken进行查询，就可以查到一个请求的完整链路，而且还可以根据currentOrder字段查看调用顺序，1为首次请求，2为第二次，依次递加。

![image](https://user-images.githubusercontent.com/44285123/236731626-f644d0c4-aca0-4695-afa0-490a34fbfd0f.png)

### 链路追踪配置

在创建HTTP请求时，必须先在请求头header附上tracetoken和currentOrder，不然无法实现链路追踪。请参考下面代码：

```
private HttpHeaders createHttpHeaders() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(LogVariableKey.TRACE_TOKEN, LogPackageHolder.getCurrentTraceToken());
    httpHeaders.add(LogVariableKey.TRACE_TOKEN, LogPackageHolder.getCurrentOrder().toString());
    return httpHeaders;
}
```





## 其他说明



### SQL语句日志

此项目并未记录SQL日志，如工程中有sql相关的记录，请调用下面方法将SQL信息添加到日志

```
LogPackageHolder.addSQL(sqltext); // sqltext指具体sql信息
```

![image](https://user-images.githubusercontent.com/44285123/236731896-e1e47499-127a-4085-abc1-78de5ae6a343.png)
