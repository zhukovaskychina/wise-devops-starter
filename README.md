# wise-devops-starter
### 项目背景
   企业产品化，以半开源的形式部署jar包到客户的服务器上，客户往往会有定制化的需求，为了不暴露所有的代码，或者以极小的代价满足客户的个性需求，如页面上增加一个按钮
在不修改标准产品代码的基础上，我们只要对产品做一次增强即可。也就是将定制化的jar包部署到指定目录上，达到覆盖原有的http接口，或者是改写原有的java service的内部代码。



##### 场景：
1，标品库为定型了的产品，定制化库为用户二次开发。
比如标准产品库使用的rabbit-mq，用户需要将rabbit-mq替换为rocket-mq客户端。在不破坏标品库代码的情况下，用户只要根据相关Spring的规范，实现相关代码逻辑即可。

2，比如标品库的某个接口，用户需要二次开发，增强原有的接口功能，不更改标品库的代码，只需要引入wise-devops-spring-boot-starter,做好相关配置即可，在定制化库中
实现原有接口的增强逻辑。


##### 技术实现
#####整体架构
![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/archs.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/archs.png?raw=true)
我们就是基于Classloader定制化，实现了不同硬盘路径同一classpath的jar加载，从而给标准产品增加了二次开发的可能。

##### Spring Bean生命周期
![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/spring-beans1.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/spring-beans1.png?raw=true)
![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/spring-beans2.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/spring-beans2.png?raw=true)

##### Spring Bean PostProcessor

![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/spring_autowired.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/spring_autowired.png?raw=true)

#### Spring 
### 如何使用
##### 1，pom.xml的修改
在Spring Boot项目中pom.xml引入如下配置
```xml
    <dependency>
       <groupId>io.github.zhukovaskychina</groupId>
       <artifactId>wise-devops-spring-boot-starter</artifactId>
       <version>0.0.4</version>
    </dependency>
```

##### 2，java代码修改
在启动类中，将

`
SpringApplication.run(HrmsApplication.class,args);
`

替换为:

`
SpringBootDevOpsApplication.run(HrmsApplication.class,args);
`
;

在启动类上增加如下配置，
basePackage一定要配置正确，必须要和定制化库里的包名前缀要一致。

```java
@SpringBootApplication
@ComponentScan(basePackages ="com.zhukovasky",
excludeFilters = {
@ComponentScan.Filter(type = FilterType.CUSTOM,classes = {CustomerRestControllerFilter.class})
}
)
```
最终效果如下：

```java
@SpringBootApplication
@ComponentScan(basePackages ="com.zhukovasky",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.CUSTOM,classes = {CustomerRestControllerFilter.class})
        }
)
public class HrmsApplication {

    public static void main(String[] args) {
        SpringBootDevOpsApplication.run(HrmsApplication.class,args);
    }
}
```

##### 3，配置文件修改

application.yaml
```yaml

# devops相关配置
devops:
   # 开启devops模式
   enableDevopsMode: true 
   # 开启bean过滤模式，用于过滤掉标品库不需要的restcontroller
   enableBeanFilter: true
   # 开启bean交换,用于交换service的实现类，让定制化库的service实例优先加载，相当于@Primary注解修饰
   enableBeanSwap: true
   # customDir 定制化的 
   customDir: D:/ideaprojects/hrms/hrms-ext-libs/
   # scanPackge 标品化库需要热替换的bean，以“;”为分隔符，写多个
   scanPackage: com.zhukovasky.hrms.serviceimpl
   # 
   restFilterScanPackage: com.zhukovasky.hrms.rest.IndexRest;com.zhukovasky.hrms.rest.ChildIndexRest


```
##### 4，客制化代码
![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/custom_ext_dev.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/custom_ext_dev.png?raw=true)
在resources/META-INF下增加如下配置
```properties
loginServiceImpl=loginExtServiceImpl
authCheckServiceImpl=authCheckExtServiceImpl

```
其他部分正常开发。
### 一个例子
   [hrms 拓展例子](https://github.com/zhukovaskychina/hrms-demo.git)

### service接口替换效果演示
###### 标品库中，LoginService原有的实现接口
  ![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/img_production.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/img_production.png?raw=true)

###### 二次开发，二次开发loginService接口，继承标品库的LoginService接口
  ![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_ext_dev.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_ext_dev.png?raw=true)

###### 测试效果
![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_service_swap.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_service_swap.png?raw=true)

###### postman返回效果
![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_service_swap_result1.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_service_swap_result1.png?raw=true)

##### controller接口二次开发覆盖
###### 标品库中，/index/login原有的实现以及调用效果
![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_rest.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_rest.png?raw=true)
![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_origin.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_origin.png?raw=true)
###### 定制化库，/index/login改造的实现
![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_rest_ext.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/hrms_rest_ext.png?raw=true)

![https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/img_replace.png?raw=true](https://github.com/zhukovaskychina/wise-devops-starter/blob/main/pictures/img_replace.png?raw=true)

### RoadMap
- [X] 支持不同硬盘路径，同一ClassPath路径
- [X] 支持定制化库定制service接口开发
- [ ] 增加对Nacos和Apollo的支持
- [ ] 增加对微服务的支持测试
- [ ] 热部署

