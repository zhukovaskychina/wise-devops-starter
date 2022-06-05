package com.wiseoutbound.postprocessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

public class CustomDevopsBeanPostProcessor implements BeanFactoryPostProcessor {
    /***
     * 改写Spring关于JSR-330‘s javax.inject.Qualifier的支持
     * Spring 在正常情况下，Qualifier和Autowired注解使用有优先级，Qualifier大于Autowired
     * 因此有必要在这里让Qualifier在标品库中，有客制化代码覆盖的情况下，直接加载客制化库代码中的具体实现
     * **/
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        ContextStrengthAnnotationAutowiredResolver contextStrengthAnnotationAutowiredResolver=new ContextStrengthAnnotationAutowiredResolver();
        ((DefaultListableBeanFactory)beanFactory).setAutowireCandidateResolver(contextStrengthAnnotationAutowiredResolver);

    }
}
