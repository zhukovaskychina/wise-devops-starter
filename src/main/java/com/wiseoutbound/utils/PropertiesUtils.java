package com.wiseoutbound.utils;

import com.wiseoutbound.utils.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.wiseoutbound.consts.SystemParams.*;
import static com.wiseoutbound.consts.SystemParams.PROFILE_ACTIVE;

@Slf4j
public class PropertiesUtils {
    /***
     *
     * 读取指定jar包当中的文件
     * @param  jar  指定jar包
     * @param propertyFileName 属性文件名称
     * ****/
    public static Properties readPropertiesFromJar(JarFile jar, String propertyFileName) throws IOException {
        Enumeration<JarEntry> en = jar.entries();
        Properties props = new Properties();
        while (en.hasMoreElements()) {
            JarEntry je = en.nextElement();
            String name = je.getName();
            if (StringUtils.equals(name, propertyFileName)) {
                InputStream input = jar.getInputStream(je);
                props.load(input);
                input.close();
            }

        }
        return props;
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
