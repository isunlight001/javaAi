package com.zhipu.demo.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class ValueArrayConfig {

    // 通过@Value注解获取String数组
    @Value("${app.test-array:default1,default2,default3}")
    private String[] testArray;

    // 通过@Value注解获取List
    @Value("${app.test-list:defaultA,defaultB,defaultC}")
    private List<String> testList;

    // 静态变量用于存储数组值
    private static String[] staticTestArray;
    
    // 静态变量用于存储List值
    private static List<String> staticTestList;

    @PostConstruct
    public void init() {
        // 在Bean初始化时将值赋给静态变量
        staticTestArray = this.testArray;
        staticTestList = this.testList;
        
        log.info("从配置文件加载的数组值: {}", Arrays.toString(testArray));
        log.info("从配置文件加载的列表值: {}", testList);
    }

    // 提供静态方法访问数组值
    public static String[] getStaticTestArray() {
        return staticTestArray;
    }

    // 提供静态方法访问列表值
    public static List<String> getStaticTestList() {
        return staticTestList;
    }
    
    // 提供实例方法访问数组值
    public String[] getTestArray() {
        return testArray;
    }
    
    // 提供实例方法访问列表值
    public List<String> getTestList() {
        return testList;
    }
}