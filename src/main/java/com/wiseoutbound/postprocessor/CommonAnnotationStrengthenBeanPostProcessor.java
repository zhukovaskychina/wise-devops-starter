package com.wiseoutbound.postprocessor;


import com.wiseoutbound.utils.ThreadLocalUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;

import java.util.Map;

public class CommonAnnotationStrengthenBeanPostProcessor extends CommonAnnotationBeanPostProcessor
        implements ApplicationContextAware, ApplicationListener<ApplicationStartedEvent> {


    private boolean started=false;

    private ApplicationContext applicationContext;

    private Map<String,String> hotSwapBeanMap;


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!started){
            return super.postProcessAfterInitialization(bean,beanName);
        }
        if (this.hotSwapBeanMap.containsKey(beanName)){
            bean=this.applicationContext.getBean(this.hotSwapBeanMap.get(beanName));
            beanName=this.hotSwapBeanMap.get(beanName);
        }

        return super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.applicationContext=applicationContext;
         this.hotSwapBeanMap= ThreadLocalUtils.getThreadLocalHotSwapBeanMap();
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
         if (!(event instanceof ApplicationStartedEvent)){
             return;
         }
         this.started=true;
    }
}
