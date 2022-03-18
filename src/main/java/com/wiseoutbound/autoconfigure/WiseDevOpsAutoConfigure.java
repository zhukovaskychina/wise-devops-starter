package com.wiseoutbound.autoconfigure;

import com.wiseoutbound.classloader.hotswapbeans.BeanExtensionHotSwapLoader;
import com.wiseoutbound.postprocessor.AutowiredStrengthenAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
