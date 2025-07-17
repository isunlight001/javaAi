package com.zhipu.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 启动运行器
 * 在应用启动完成后执行启动日志记录
 */
@Slf4j
@Component
public class StartupRunner implements CommandLineRunner {

    private final Environment environment;

    public StartupRunner(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("==========================================");
        log.info("应用启动完成 - CommandLineRunner");
        log.info("应用名称: {}", environment.getProperty("spring.application.name"));
        log.info("端口: {}", environment.getProperty("server.port"));
        log.info("环境: {}", environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default");
        log.info("数据库URL: {}", environment.getProperty("spring.datasource.url"));
        log.info("智普AI模型: {}", environment.getProperty("zhipu.ai.model"));
        log.info("==========================================");
    }
} 