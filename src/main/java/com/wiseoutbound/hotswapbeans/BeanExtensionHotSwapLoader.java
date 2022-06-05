package com.wiseoutbound.classloader.hotswapbeans;


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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;

/****
 * BeanExtensionHotSwapLoader 加载本地classpath上面的接口实现类型，用于替代
 * 原先定义接口的bean的类实现，比如service层面的更改，
 * 暂时不支持对ORM层面的更改
 * ******/
public class BeanExtensionHotSwapLoader implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(BeanExtensionHotSwapLoader.class);
    private static final String EXT_PROPERTIES = "META-INF/swap_devops_beans.properties";
    private ApplicationContext context;
    private String dir;

    private Properties finalProperties;

    public BeanExtensionHotSwapLoader() {
        this.finalProperties = new Properties();
    }

    /**
     * 将dir下面的jar包注册到classpath中，并且实现类加载和注册
     **/
    public void register(String dir) throws IOException {
        if (StringUtils.isEmpty(dir)) {
            return;
        }
        this.dir = dir;
        logger.info("=========>begin to register" + dir + "");
        synchronized (this) {
            loadMetaSpiProps();
        }
        logger.info("=========>end to register" + dir + "");
    }

    /****
     *
     * 加载META-INF下面的配置文件
     * **/
    private void loadMetaSpiProps(String  skipListJars) throws IOException {
        List<String> list=DevOpsClassLoaderUtils.getSkipJarList(skipListJars);

        Collection<File> jarListFiles = FileUtils.listFiles(new File(this.dir), new String[]{"jar"}, true);
        for (File currentJarFile : jarListFiles) {
            if (DevOpsClassLoaderUtils.isInSkippedList(cu))
            JarFile jar = new JarFile(currentJarFile);
            Properties properties = PropertiesUtils.readPropertiesFromJar(jar, EXT_PROPERTIES);
            finalProperties.putAll(properties);
        }

    }


    public void hotSwapBeans() {
        int cnt=0;
        for (Map.Entry<Object, Object> obj : this.finalProperties.entrySet()) {
            if (obj.getKey() instanceof String) {
                String originName = (String) obj.getKey();

                if (!(obj.getValue() == null)) {

                    assert obj.getValue() instanceof String;
                    String replaceName = (String) obj.getValue();
                    BeanDefinition originBeanDefinition = getRegistry().getBeanDefinition((String) obj.getKey());

                    if (originBeanDefinition == null) {
                        throw new NoSuchBeanDefinitionException("NoBeanDefinition beanName:" + originName);
                    }
                    originBeanDefinition.setPrimary(false);

                    BeanDefinition replaceBeanDefinition = getRegistry().getBeanDefinition(replaceName);
                    if (replaceBeanDefinition == null) {
                        throw new NoSuchBeanDefinitionException("NoBeanDefinition beanName:" + replaceName);
                    }
                    replaceBeanDefinition.setPrimary(true);
                    getRegistry().removeBeanDefinition(originName);
                    getRegistry().removeBeanDefinition(replaceName);

                    getRegistry().registerBeanDefinition(originName, originBeanDefinition);
                    getRegistry().registerBeanDefinition(replaceName, replaceBeanDefinition);

                    cnt++;
                }

            }

        }
        logger.info("We have swapped "+ cnt+ " pairs of beans");
    }

    public BeanDefinitionRegistry getRegistry() {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) context;
        //  ((ConfigurableApplicationContext) context).refresh();

        return (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

}

