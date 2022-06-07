package com.wiseoutbound.wrapper;


import com.wiseoutbound.hotswapbeans.BeanExtensionHotSwapLoader;
import com.wiseoutbound.utils.DevOpsClassLoaderUtils;
import com.wiseoutbound.utils.ReflectionUtils;
import com.wiseoutbound.utils.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static com.wiseoutbound.consts.SystemParams.*;
import static com.wiseoutbound.utils.PropertiesUtils.getDevOpsProperties;

@Slf4j
public class SpringBootDevOpsApplication {




    public static ConfigurableApplicationContext run(Class<?> primarySource, String[] args) {
        Properties devOpsProperties = null;
        try {
            devOpsProperties = getDevOpsProperties();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
        String isEnableDevOps = devOpsProperties.getProperty(ENABLE_DEVOPS_MODE);
        if (Boolean.parseBoolean(isEnableDevOps)) {
            log.info("Now,we begin to the dynamic devops mode");
            return runDevOps(primarySource, args, devOpsProperties);
        } else {
            return SpringApplication.run(primarySource, args);
        }
    }


    public static ConfigurableApplicationContext runDevOps(Class<?> primarySource, String[] args, Properties devOpsProperties) {


        String filterSkipJars=devOpsProperties.getProperty(FILTER_SCAN_PACKAGE);

        ClassLoader devOpsClassLoader = DevOpsClassLoaderUtils.getClassLoader(devOpsProperties.getProperty(DEVOPS_URL),filterSkipJars);

        /**
         *
         * 设置上下文ClassLoader
         * **/
        Thread.currentThread().setContextClassLoader(devOpsClassLoader);

        String className = SpringApplication.class.getName();
        Method runMethod = ReflectionUtils.getMethod(devOpsClassLoader, className, "run", Class.class, String[].class);

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) ReflectionUtils.invoke(runMethod, primarySource, args);



        return context;
    }

    static class PrepareDevops{
        private static PrepareDevops instance;

        private PrepareDevops() {

        }
        public static PrepareDevops getInstance() {
            if(instance == null){
                return new PrepareDevops();
            }
            return instance;
        }

        void prepare(Properties devopsProperties,String filterSkipJars){
            if (Boolean.parseBoolean(devopsProperties.getProperty(ENABLE_BEAN_SWAP))){
                log.warn("你开启了试验性质的Bean替换模式，如果有bug，请在客制化代码中接口实现类使用注解@Primary修饰你的类");
                log.info("Now,we have started the dynamic devops mode successfully!");
                log.info("Now,we have started spring beans hotswap phase");
                BeanExtensionHotSwapLoader beanExtensionHotSwapLoader = new BeanExtensionHotSwapLoader();
                try {
                    beanExtensionHotSwapLoader.register(devopsProperties.getProperty(DEVOPS_URL),filterSkipJars);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                log.info("Now,spring beans hotswap successfully");
            }else{
                ThreadLocalUtils.setThreadLocalHotSwapBeanMap(new ConcurrentHashMap<>());
            }
        }
    }

}
