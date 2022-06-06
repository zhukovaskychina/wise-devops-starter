package com.wiseoutbound.autoconfigure;

import com.wiseoutbound.postprocessor.CommonAnnotationStrengthenBeanPostProcessor;
import com.wiseoutbound.postprocessor.CustomDevopsBeanPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import static com.wiseoutbound.consts.SystemParams.ENABLE_DEVOPS_MODE;

@Configuration
@Role(RootBeanDefinition.ROLE_INFRASTRUCTURE)
public class WiseDevOpsAutoConfigure {



    @Bean
    @ConditionalOnProperty(value=ENABLE_DEVOPS_MODE,havingValue = "true")
    public CustomDevopsBeanPostProcessor initCustomDevopsBeanPostProcessor(){
        return new CustomDevopsBeanPostProcessor();
    }

    @Bean
    @ConditionalOnProperty(value=ENABLE_DEVOPS_MODE,havingValue = "true")
    public CommonAnnotationStrengthenBeanPostProcessor initCommonAnnotationStrengthBeanPostProcessor(){
        return new CommonAnnotationStrengthenBeanPostProcessor();
    }

}
