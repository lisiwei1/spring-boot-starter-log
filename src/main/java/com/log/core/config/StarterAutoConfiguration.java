package com.log.core.config;

import com.log.core.log.customHandle.CustomLogExceptionHandler;
import com.log.core.log.customHandle.DefaultCustomExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author lsw
 * @Date 2023/10/16 21:34
 * @Description
 */
@Configuration
public class StarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CustomLogExceptionHandler customLogExceptionHandler(){
        return new DefaultCustomExceptionHandler();
    }

}
