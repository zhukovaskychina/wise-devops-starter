package com.wiseoutbound.nacos;

import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class NacosConfigService {

    public static Map<String,Object> getNacosConfigFromRemoteServer(String profiles) throws IOException, NacosException {
        Map<String,Object> dataMap=new HashMap<>();

        MutablePropertySources propertySources=new MutablePropertySources();

        final ConfigurablePropertyResolver propertyResolver=new PropertySourcesPropertyResolver(propertySources);

        PropertySourceLoader loader = new YamlPropertySourceLoader();

        List<PropertySource<?>> list=loader.load("bootstrap.yml",new ClassPathResource("bootstrap.yml"));
        for (int i = 0; i < list.size(); i++){
            propertySources.addLast(list.get(i));
        }
        String applicationName=propertyResolver.resolvePlaceholders("${spring.application.name}");


        Properties nacosProperties=new Properties();

        for (PropertySource< ? > propertySource : propertySources) {
            Object value = propertySource.getProperty("spring.profiles");
            if (value!=null){
                if (StringUtils.equals(profiles,String.valueOf(value))){
                    String userName= (String) propertySource.getProperty("spring.cloud.nacos.username");
                    String password= (String) propertySource.getProperty("spring.cloud.nacos.password");

                    Map<String,String> envMap=System.getenv();

                    String serverAddr= (String) propertySource.getProperty("spring.cloud.nacos.config.server-addr");

                    String namespace= (String) propertySource.getProperty("spring.cloud.nacos.config.namespace");

                    nacosProperties.setProperty(PropertyKeyConst.USERNAME,userName);
                    nacosProperties.setProperty(PropertyKeyConst.PASSWORD,password);
                    nacosProperties.setProperty(PropertyKeyConst.SERVER_ADDR,serverAddr);
                    nacosProperties.setProperty(PropertyKeyConst.NAMESPACE,namespace);
                    break;
                }
            }
        }

        String dataId="application.yml";
        ConfigService configService= NacosFactory.createConfigService(nacosProperties);
        String content=configService.getConfig(dataId,applicationName,5000);

        dataMap=NacosDataParserHandler.getInstance().parseNacosData(content,"yml");

        return dataMap;
    }
}
