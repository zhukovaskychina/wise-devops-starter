package com.wiseoutbound.autoconfigure;

import com.wiseoutbound.classloader.hotswapbeans.BeanExtensionHotSwapLoader;
import com.wiseoutbound.postprocessor.AutowiredStrengthenAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@Role(RootBeanDefinition.ROLE_INFRASTRUCTURE)
public class WiseDevOpsAutoConfigure {


    @Bean
    public BeanExtensionHotSwapLoader getBeanExtensionHotSwapLoader() {
        BeanExtensionHotSwapLoader beanExtensionHotSwapLoader = new BeanExtensionHotSwapLoader();
        return beanExtensionHotSwapLoader;
    }

    @Bean
    public AutowiredStrengthenAnnotationBeanPostProcessor getAutowiredStrengthenAnnotationBeanPostProcessor() {
        return new AutowiredStrengthenAnnotationBeanPostProcessor();
    }

}
