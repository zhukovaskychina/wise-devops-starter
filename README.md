# wise-devops-starter
### 项目背景
   本项目以自定义加载类Classloader，实现了不同硬盘路径，同一ClassPath的包加载，最终实现了
Spring Context加载。


##### 场景：
1，标品库为定型了的产品，定制化库为用户二次开发。
比如标准产品库使用的rabbit-mq，用户需要将rabbit-mq替换为rocket-mq客户端。在不破坏标品库代码的情况下，用户只要根据相关Spring的规范，实现相关代码逻辑即可。

2，比如标品库的某个接口，用户需要二次开发，增强原有的接口功能，不更改标品库的代码，只需要引入wise-devops-spring-boot-starter,做好相关配置即可，在定制化库中
实现原有接口的增强逻辑。


### 如何使用
##### pom.xml的修改
在Spring Boot项目中pom.xml引入如下配置
```
    <dependency>
       <groupId>io.github.zhukovaskychina</groupId>
       <artifactId>wise-devops-spring-boot-starter</artifactId>
       <version>0.0.4</version>
    </dependency>
```

##### java代码修改
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

```
@SpringBootApplication
@ComponentScan(basePackages ="com.zhukovasky",
excludeFilters = {
@ComponentScan.Filter(type = FilterType.CUSTOM,classes = {CustomerRestControllerFilter.class})
}
)
```
最终效果如下：

```
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

##### 配置文件修改

application.yaml
```

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

### 一个例子
   [hrms 拓展例子](https://github.com/zhukovaskychina/hrms-demo.git)

##### service接口替换效果演示
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

