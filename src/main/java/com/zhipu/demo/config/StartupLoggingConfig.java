package com.zhipu.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class StartupLoggingConfig implements BeanPostProcessor, ApplicationContextAware {
    private final Map<String, Long> beanStartTimes = new ConcurrentHashMap<>();
    private final Map<String, Long> beanLoadTimes = new ConcurrentHashMap<>();
    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        beanStartTimes.put(beanName, System.currentTimeMillis());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        long end = System.currentTimeMillis();
        Long start = beanStartTimes.get(beanName);
        if (start != null) {
            beanLoadTimes.put(beanName, end - start);
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public Map<String, Long> beanLoadTimes() {
        return beanLoadTimes;
    }
} 