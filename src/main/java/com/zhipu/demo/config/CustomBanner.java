package com.zhipu.demo.config;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 * 自定义启动Banner
 */
public class CustomBanner implements Banner {

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.println();
        out.println("==========================================");
        out.println("智普AI + MySQL 集成 Demo");
        out.println("==========================================");
        out.println("应用名称: " + environment.getProperty("spring.application.name"));
        out.println("端口: " + environment.getProperty("server.port", "8080"));
        out.println("环境: " + (environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default"));
        out.println("==========================================");
        out.println();
    }
} 