# wise-devops-starter
### 项目背景
   本项目以自定义加载类Classloader，实现了不同硬盘路径，同一ClassPath的包加载，最终实现了
Spring Context加载。




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
-[ ]




### RoadMap
- [X] 支持不同硬盘路径，同一ClassPath路径
- [X] 支持定制化库定制service接口开发
- [ ] 增加对Nacos和Apollo的支持
- [ ] 增加对微服务的支持测试

