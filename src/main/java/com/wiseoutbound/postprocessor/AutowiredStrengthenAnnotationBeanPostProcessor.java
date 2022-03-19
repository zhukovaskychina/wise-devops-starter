package com.wiseoutbound.postprocessor;

import com.wiseoutbound.classloader.utils.ThreadLocalUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.wiseoutbound.consts.SystemParams.*;

/****
 * 用于实现Autowired 的Bean增强
 *
 * ***/
public class AutowiredStrengthenAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor implements ApplicationContextAware {
    private static final String LIST_SEPARATOR = ";";

    private ApplicationContext applicationContext;
    private Map<String, Object> devopsMap;

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();


    @Override
    public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeanCreationException {

        if (beanDefinitionMap.containsKey(beanName)) {
            Class<?>[] interfaceList = beanClass.getInterfaces();
            for (int i = 0; i < interfaceList.length; i++) {
                Map<String, ?> autowiredMapInterface = this.findAutowireCandidates(interfaceList[i]);
                Set<String> keys = autowiredMapInterface.keySet();

                for (String key : keys) {
                    BeanDefinition beanDefinition = this.getRegistry().getBeanDefinition(key);
                    if (beanDefinition.isPrimary()) {
                        beanName = key;
                        Object beanValue = this.applicationContext.getBean(beanName);
                        beanClass = beanValue.getClass();
                    }
                }
            }

            try {
                return new Constructor[]{beanClass.getConstructor(null)};
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return super.determineCandidateConstructors(beanClass, beanName);
    }

    @Override
    public Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException {

        if (((Boolean)this.devopsMap.get(ENABLE_DEVOPS_MODE)==true)&&((Boolean) this.devopsMap.get(ENABLE_BEAN_SWAP))==true) {
            String beanClassName = beanClass.getName();
            List<String> stringList = (List<String>) this.devopsMap.get(SCANPACKAGE);

            for (String stringStr : stringList) {
                if (StringUtils.isEmpty(stringStr)) {
                    continue;
                }
                if (StringUtils.startsWith(beanClassName, stringStr)) {
                    if (!this.beanDefinitionMap.containsKey(beanName)) {
                        BeanDefinition beanDefinition = this.getRegistry().getBeanDefinition(beanName);
                        beanDefinition.setPrimary(true);
                        this.getRegistry().removeBeanDefinition(beanName);
                        this.getRegistry().registerBeanDefinition(beanName, beanDefinition);
                        this.beanDefinitionMap.put(beanName, beanDefinition);
                    }
                }
            }
        }
        return super.predictBeanType(beanClass, beanName);
    }

    /**
     * 用于实现BeanDefinition的增强
     * 将指定的包下面的bean 做primary处理，为了解决报错问题
     ***/
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {

        if (((Boolean)this.devopsMap.get(ENABLE_DEVOPS_MODE)==true)&&((Boolean) this.devopsMap.get(ENABLE_BEAN_SWAP))==true) {
            String beanClassName = beanClass.getName();
            List<String> stringList = (List<String>) this.devopsMap.get(SCANPACKAGE);

            for (String stringStr : stringList) {
                if (StringUtils.isEmpty(stringStr)) {
                    continue;
                }
                if (StringUtils.startsWith(beanClassName, stringStr)) {
                    if (!this.beanDefinitionMap.containsKey(beanName)) {
                        BeanDefinition beanDefinition = this.getRegistry().getBeanDefinition(beanName);
                        if (!this.applicationContext.containsBean(beanName)) {
                            beanDefinition.setPrimary(true);
                            this.getRegistry().removeBeanDefinition(beanName);
                            this.getRegistry().registerBeanDefinition(beanName, beanDefinition);
                            this.beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    }
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
        if (properties.containsKey(ENABLE_BEAN_SWAP)) {
            this.devopsMap.put(ENABLE_BEAN_SWAP, Boolean.parseBoolean(properties.getProperty(ENABLE_BEAN_SWAP)));
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
