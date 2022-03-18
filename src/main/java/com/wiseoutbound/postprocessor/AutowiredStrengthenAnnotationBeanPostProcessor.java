package com.wiseoutbound.postprocessor;

import com.wiseoutbound.classloader.utils.ThreadLocalUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

/****
 * 用于实现Autowired 的Bean增强
 *
 * ***/
public class AutowiredStrengthenAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor implements ApplicationContextAware {
    private static final String LIST_SEPARATOR = ";";
    private static final String SCANPACKAGE = "devops.scanPackage";
    private static final String ENABLE_DEVOPS_MODE = "devops.enable";
    private ApplicationContext applicationContext;
    private Map<String, Object> devopsMap;

    /**
     * 用于实现BeanDefinition的增强
     * 将指定的包下面的bean 做primary处理，为了解决报错问题
     ***/
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        if ((Boolean) this.devopsMap.get(ENABLE_DEVOPS_MODE)) {

            String beanClassName = beanClass.getName();
            List<String> stringList = (List<String>) this.devopsMap.get(SCANPACKAGE);

            for (String stringStr : stringList) {
                if (StringUtils.isEmpty(stringStr)) {
                    continue;
                }
                if (StringUtils.startsWith(beanClassName, stringStr)) {
                    BeanDefinition beanDefinition = this.getRegistry().getBeanDefinition(beanName);
                    beanDefinition.setPrimary(true);
                    this.getRegistry().removeBeanDefinition(beanName);
                    this.getRegistry().registerBeanDefinition(beanName, beanDefinition);
                }
            }
        }
        return super.postProcessBeforeInstantiation(beanClass, beanName);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.devopsMap = new HashMap<>();
        Properties properties = ThreadLocalUtils.getDevopsProperties();

        if (properties.containsKey(ENABLE_DEVOPS_MODE)) {
            this.devopsMap.put(ENABLE_DEVOPS_MODE, Boolean.parseBoolean(properties.getProperty(ENABLE_DEVOPS_MODE)));
        }
        if (properties.containsKey(SCANPACKAGE)) {
            String scanList = properties.getProperty(SCANPACKAGE);
            String[] list = StringUtils.split(scanList, LIST_SEPARATOR);
            this.devopsMap.put(SCANPACKAGE, Arrays.stream(list).collect(Collectors.toList()));
        }
        this.applicationContext = applicationContext;
    }

    public BeanDefinitionRegistry getRegistry() {
        ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) this.applicationContext;
        //  ((ConfigurableApplicationContext) context).refresh();

        return (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();
    }

}
