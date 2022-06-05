package com.wiseoutbound.postprocessor;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;

import com.wiseoutbound.utils.ThreadLocalUtils;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.wiseoutbound.consts.SystemParams.*;

public class ContextStrengthAnnotationAutowiredResolver
extends ContextAnnotationAutowireCandidateResolver
{

    private Set<String> targetNoPrimaryBeansSet=new ConcurrentHashSet<>();

    private Set<String> targetPrimaryBeansSet=new ConcurrentHashSet<>();

    private Map<String,Object> devopsMap;

    public ContextStrengthAnnotationAutowiredResolver(){
        this.devopsMap=new ConcurrentHashMap<>();
        Properties properties= ThreadLocalUtils.getDevopsProperties();
        if (properties==null){
            return;
        }
        this.devopsMap.put(ENABLE_DEVOPS_MODE,Boolean.parseBoolean(properties.getProperty(ENABLE_DEVOPS_MODE)));
        this.devopsMap.put(ENABLE_BEAN_SWAP,Boolean.parseBoolean(properties.getProperty(ENABLE_BEAN_SWAP)));

        Map<String,String> swapBeanMaps=ThreadLocalUtils.getThreadLocalHotSwapBeanMap();
        if (swapBeanMaps==null){
            swapBeanMaps=new ConcurrentHashMap<>();
        }
        this.targetNoPrimaryBeansSet=swapBeanMaps.keySet();
        this.targetPrimaryBeansSet=swapBeanMaps.values().stream().collect(Collectors.toSet());
    }

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
         boolean enableDevops=(Boolean)this.devopsMap.get(ENABLE_DEVOPS_MODE);
         boolean enableBeanSwap=(Boolean)this.devopsMap.get(ENABLE_BEAN_SWAP);
         if ((enableDevops==true)&&(enableBeanSwap==true)){
             String beanName=bdHolder.getBeanName();
             if (targetNoPrimaryBeansSet.contains(beanName)){
                 bdHolder.getBeanDefinition().setPrimary(false);
             }
             if (targetPrimaryBeansSet.contains(beanName)){
                 bdHolder.getBeanDefinition().setAutowireCandidate(true);
                 bdHolder.getBeanDefinition().setPrimary(true);
             }
         }

        return super.isAutowireCandidate(bdHolder, descriptor);
    }
}
