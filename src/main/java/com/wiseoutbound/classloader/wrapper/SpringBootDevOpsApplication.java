package com.wiseoutbound.classloader.wrapper;


import com.wiseoutbound.classloader.hotswapbeans.BeanExtensionHotSwapLoader;
import com.wiseoutbound.classloader.utils.DevOpsClassLoaderUtils;
import com.wiseoutbound.classloader.utils.ReflectionUtils;
import com.wiseoutbound.classloader.utils.ThreadLocalUtils;
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

import static com.wiseoutbound.consts.SystemParams.*;

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


        ClassLoader devOpsClassLoader = DevOpsClassLoaderUtils.getClassLoader(devOpsProperties.getProperty(DEVOPS_URL));

        /**
         *
         * 设置上下文ClassLoader
         * **/
        Thread.currentThread().setContextClassLoader(devOpsClassLoader);

        String className = SpringApplication.class.getName();
        Method runMethod = ReflectionUtils.getMethod(devOpsClassLoader, className, "run", Class.class, String[].class);

        ConfigurableApplicationContext context = (ConfigurableApplicationContext) ReflectionUtils.invoke(runMethod, primarySource, args);



        if (Boolean.parseBoolean(devOpsProperties.getProperty(ENABLE_BEAN_SWAP))){
            log.warn("你开启了试验性质的Bean替换模式，如果有bug，请在客制化代码中接口实现类使用注解@Primary修饰你的类");
            log.info("Now,we have started the dynamic devops mode successfully!");
            log.info("Now,we have started spring beans hotswap phase");
            BeanExtensionHotSwapLoader beanExtensionHotSwapLoader = context.getBean(BeanExtensionHotSwapLoader.class);
            try {
                beanExtensionHotSwapLoader.register(devOpsProperties.getProperty(DEVOPS_URL));
            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (beanExtensionHotSwapLoader) {
                beanExtensionHotSwapLoader.hotSwapBeans();
            }
            log.info("Now,spring beans hotswap successfully");
        }



        return context;
    }

    public static Properties getDevOpsProperties() throws NoSuchFieldException {
        Properties properties = new Properties();
        PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("systemProperties", System.getProperties());
        //读取application-{profile}配置
        if (propertiesPropertySource.containsProperty(PROFILE_ACTIVE)) {
            String profiles = (String) propertiesPropertySource.getProperty(PROFILE_ACTIVE);
            getApplicationProfileProperties(properties, profiles);
        } else if (propertiesPropertySource.containsProperty("spring.config.location")) {
            String locations = (String) propertiesPropertySource.getProperty("spring.config.location");
            processWithConfigLocations(properties, locations);
        } else {
            processWithoutProfiles(properties);
        }
        checkProperties(properties);
        log.info("devops.properties=" + properties.toString());
        ThreadLocalUtils.setDevopsProperties(properties);
        return properties;
    }

    private static void processWithConfigLocations(Properties properties, String locations) {
        assert locations != null;
        if (StringUtils.startsWith(locations, "classpath:")) {
            if (StringUtils.endsWith(locations, ".properties")) {
                ClassPathResource classPathResource = new ClassPathResource(locations);
                extractDevOpsResource(classPathResource, properties);
            } else {
                ClassPathResource applicationYamlFile = new ClassPathResource(locations);
                Properties appProperties = getYamlProperties(applicationYamlFile);
                extractDevOpsProperties(properties, appProperties);
            }
        } else {
            Collection<File> listFiles = FileUtils.listFiles(new File(locations), new String[]{"properties", "yml", "yaml"}, false);
            for (File file : listFiles) {
                String fileName = file.getName();
                if (StringUtils.endsWith(fileName, ".properties")) {
                    Resource resource = null;

                    try {
                        resource = new FileUrlResource(file.getAbsolutePath());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    extractDevOpsResource(resource, properties);
                } else {
                    try {
                        Resource resource = new FileUrlResource(file.getAbsolutePath());
                        Properties appProperties = getYamlProperties(resource);
                        extractDevOpsProperties(properties, appProperties);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private static void processWithoutProfiles(Properties properties) {
        getApplicationConfig(properties);
        if (!StringUtils.isEmpty(properties.getProperty(PROFILE_ACTIVE))) {
            String profile = properties.getProperty(PROFILE_ACTIVE);
            getApplicationProfileProperties(properties, profile);
        } else {
            getApplicationConfig(properties);
            log.info("current spring profile is default");
        }
    }

    private static void checkProperties(Properties properties) throws NoSuchFieldException {
        if (!properties.containsKey(ENABLE_DEVOPS_MODE)) {
            properties.setProperty(ENABLE_DEVOPS_MODE, "false");
        }
        if (!properties.containsKey(ENABLE_BEAN_SWAP)) {
            properties.setProperty(ENABLE_BEAN_SWAP, "false");
        }
        if (!properties.containsKey(ENABLE_FILTER)) {
            properties.setProperty(ENABLE_FILTER, "false");
        }

        if (!properties.containsKey(DEVOPS_URL)) {
            properties.setProperty(DEVOPS_URL, "");
        }
        if (!properties.containsKey(SCANPACKAGE)) {
            properties.setProperty(SCANPACKAGE,"");
        }
        if (!properties.containsKey(FILTER_SCAN_PACKAGE)) {
            properties.setProperty(FILTER_SCAN_PACKAGE,"");
        }
    }

    private static void getApplicationProfileProperties(Properties properties, String profiles) {
        String applicationProfilePropertiesFileName = "application-" + profiles + ".properties";
        String applicationYamlProfilePropertiesFileName = "application-" + profiles + ".yaml";
        String applicationYmlProfilePropertiesFileName = "application-" + profiles + ".yml";


        ClassPathResource applicationPropertiesProfilesResource = new ClassPathResource(applicationProfilePropertiesFileName);
        ClassPathResource applicationProfileYamlFile = new ClassPathResource(applicationYamlProfilePropertiesFileName);
        ClassPathResource applicationProfileYmlFile = new ClassPathResource(applicationYmlProfilePropertiesFileName);


        if (applicationProfileYmlFile.exists()) {  //application-profile.yml
            Properties appProperties = getYamlProperties(applicationProfileYmlFile);
            extractDevOpsProperties(properties, appProperties);
        }
        if (applicationProfileYamlFile.exists()) {    //application-profile.yaml
            Properties appProperties = getYamlProperties(applicationProfileYamlFile);
            extractDevOpsProperties(properties, appProperties);
        }
        if (applicationPropertiesProfilesResource.exists()) {  //application-profile.properties
            extractDevOpsResource(applicationPropertiesProfilesResource, properties);
        }
    }

    private static void getApplicationConfig(Properties properties) {
        String applicationPropertiesFileName = "application.properties";
        String applicationYamlFileName = "application.yaml";
        String applicationYmlFileName = "application.yml";


        //application.properties,application.yaml,application.yml
        ClassPathResource applicationPropertiesResource = new ClassPathResource(applicationPropertiesFileName);
        ClassPathResource applicationPropertiesYamlResource = new ClassPathResource(applicationYamlFileName);
        ClassPathResource applicationPropertiesYmlResource = new ClassPathResource(applicationYmlFileName);
        if (applicationPropertiesYmlResource.exists()) {  //application.yml
            Properties appProperties = getYamlProperties(applicationPropertiesYmlResource);
            extractDevOpsProperties(properties, appProperties);
        }
        if (applicationPropertiesYamlResource.exists()) {   //application.yaml
            Properties appProperties = getYamlProperties(applicationPropertiesYamlResource);
            extractDevOpsProperties(properties, appProperties);
        }

        //application.properties
        if (applicationPropertiesResource.exists()) {
            extractDevOpsResource(applicationPropertiesResource, properties);
        }

    }

    private static void extractDevOpsResource(Resource resource, Properties properties) {
        if (resource == null) {
            return;
        }
        if (!resource.exists()) {
            return;
        }
        Properties currentProperties = new Properties();
        InputStream is = null;
        try {
            is = resource.getInputStream();
            currentProperties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        extractDevOpsProperties(properties, currentProperties);
    }


    private static void extractDevOpsProperties(Properties properties, Properties currentProperties) {

        if (currentProperties.containsKey(DEVOPS_URL)) {
            properties.setProperty(DEVOPS_URL, currentProperties.getProperty(DEVOPS_URL));
        }
        if (currentProperties.containsKey(SCANPACKAGE)) {
            properties.setProperty(SCANPACKAGE, currentProperties.getProperty(SCANPACKAGE));
        }
        if (currentProperties.containsKey(ENABLE_DEVOPS_MODE)) {
            assert currentProperties.get(ENABLE_DEVOPS_MODE) instanceof Boolean;
            properties.setProperty(ENABLE_DEVOPS_MODE, currentProperties.getProperty(ENABLE_DEVOPS_MODE));
        }
        if (currentProperties.containsKey(ENABLE_BEAN_SWAP)) {
            assert currentProperties.get(ENABLE_BEAN_SWAP) instanceof Boolean;
            properties.setProperty(ENABLE_BEAN_SWAP, currentProperties.getProperty(ENABLE_BEAN_SWAP));
        }
        if (currentProperties.containsKey(ENABLE_FILTER)) {
            assert currentProperties.get(ENABLE_FILTER) instanceof Boolean;
            properties.setProperty(ENABLE_FILTER, currentProperties.getProperty(ENABLE_FILTER));
        }
        if (currentProperties.containsKey(FILTER_SCAN_PACKAGE)) {
            properties.setProperty(FILTER_SCAN_PACKAGE, currentProperties.getProperty(FILTER_SCAN_PACKAGE));
        }
        if (currentProperties.containsKey(PROFILE_ACTIVE)) {
            properties.setProperty(PROFILE_ACTIVE, currentProperties.getProperty(PROFILE_ACTIVE));
        }
    }

    private static Properties getYamlProperties(Resource classPathResource) {
        YamlPropertiesFactoryBean yamlProFb = new YamlPropertiesFactoryBean();
        yamlProFb.setResources(classPathResource);
        return yamlProFb.getObject();
    }


    public static Map<String, Object> getSystemProperties() {
        return (Map) System.getProperties();
    }
}
