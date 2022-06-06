package com.wiseoutbound.hotswapbeans;


import com.wiseoutbound.classloader.utils.PropertiesUtils;
import com.wiseoutbound.utils.DevOpsClassLoaderUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import static com.wiseoutbound.utils.DevOpsClassLoaderUtils.isInSkippedList;

/****
 * BeanExtensionHotSwapLoader 加载本地classpath上面的接口实现类型，用于替代
 * 原先定义接口的bean的类实现，比如service层面的更改，
 * 暂时不支持对ORM层面的更改
 * ******/
public class BeanExtensionHotSwapLoader  {

    private static final Logger logger = LoggerFactory.getLogger(BeanExtensionHotSwapLoader.class);
    private static final String EXT_PROPERTIES = "META-INF/swap_devops_beans.properties";
    private ApplicationContext context;
    private String dir;

    private Map<String,String> finalProperties;

    public BeanExtensionHotSwapLoader() {
        this.finalProperties = new HashMap<>();
    }

    /**
     * 将dir下面的jar包注册到classpath中，并且实现类加载和注册
     **/
    public void register(String dir,String skipListJars) throws IOException {
        if (StringUtils.isEmpty(dir)) {
            return;
        }
        this.dir = dir;
        logger.info("=========>begin to register" + dir + "");
        synchronized (this) {
            loadMetaSpiProps(skipListJars);
        }
        logger.info("=========>end to register" + dir + "");
    }

    /****
     *
     * 加载META-INF下面的配置文件
     * **/
    private Map<String,String> loadMetaSpiProps(String  skipListJars) throws IOException {
        List<String> list=DevOpsClassLoaderUtils.getSkipJarList(skipListJars);

        Collection<File> jarListFiles = FileUtils.listFiles(new File(this.dir), new String[]{"jar"}, true);
        for (File currentJarFile : jarListFiles) {
            if (isInSkippedList(currentJarFile.getName(),list)){
                continue;
            }
            JarFile jar = new JarFile(currentJarFile);
            Properties properties = PropertiesUtils.readPropertiesFromJar(jar, EXT_PROPERTIES);
            Map<String,String> propertyMap=new ConcurrentHashMap<>((Map)properties);
            finalProperties.putAll(propertyMap);
        }
        return finalProperties;
    }


}

