package com.zhipu.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * 智普AI + MySQL 集成 Demo 启动类
 */
@SpringBootApplication
@EnableScheduling
@Slf4j
public class ZhipuDemoApplication {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String startDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        log.info("=== 智普AI + MySQL 集成 Demo 启动开始 ===");
        log.info("启动时间: {}", startDateTime);
        
        try {
            SpringApplication.run(ZhipuDemoApplication.class, args);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            String endDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            log.info("=== 智普AI + MySQL 集成 Demo 启动成功 ===");
            log.info("启动完成时间: {}", endDateTime);
            log.info("总启动耗时: {} ms ({:.2f} 秒)", duration, duration / 1000.0);
            log.info("访问地址: http://localhost:8080");
            log.info("Bugfix分析工具: http://localhost:8080/bugfix.html");
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            String endDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            log.error("=== 智普AI + MySQL 集成 Demo 启动失败 ===");
            log.error("启动失败时间: {}", endDateTime);
            log.error("启动耗时: {} ms", duration);
            log.error("启动异常: {}", e.getMessage(), e);
            
            throw e;
        }
    }

    @RestControllerEndpoint(id = "startup")
    public static class StartupEndpoint {
        private final Map<String, Long> beanLoadTimes;
        public StartupEndpoint(Map<String, Long> beanLoadTimes) {
            this.beanLoadTimes = beanLoadTimes;
        }
        @GetMapping
        @ResponseBody
        public Map<String, Object> beans() {
            List<Map<String, Object>> beans = new ArrayList<>();
            for (Map.Entry<String, Long> entry : beanLoadTimes.entrySet()) {
                Map<String, Object> bean = new HashMap<>();
                bean.put("bean", entry.getKey());
                bean.put("loadTimeMs", entry.getValue());
                beans.add(bean);
            }
            beans.sort(Comparator.comparingLong(b -> (Long) b.get("loadTimeMs")));
            Map<String, Object> result = new HashMap<>();
            result.put("beanLoadTimes", beans);
            return result;
        }
    }
} 