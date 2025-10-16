package com.zhipu.demo.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@Slf4j
public class ValueArrayDemo {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ValueArrayDemo.class, args);
        
        // 演示如何在静态上下文中访问@Value加载的数组
        log.info("=== 静态访问 @Value 数组值 ===");
        String[] staticArray = ValueArrayConfig.getStaticTestArray();
        List<String> staticList = ValueArrayConfig.getStaticTestList();
        
        log.info("静态访问数组值: {}", Arrays.toString(staticArray));
        log.info("静态访问列表值: {}", staticList);
        
        // 演示直接从Spring容器获取Bean并访问值
        log.info("=== 通过Spring容器访问 @Value 数组值 ===");
        ValueArrayConfig config = context.getBean(ValueArrayConfig.class);
        
        log.info("通过Bean访问数组值: {}", Arrays.toString(config.getTestArray()));
        log.info("通过Bean访问列表值: {}", config.getTestList());
        
        // 关闭应用上下文
        context.close();
    }

    @PostConstruct
    public void postConstruct() {
        log.info("ValueArrayDemo 初始化完成");
    }
}