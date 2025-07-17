package com.zhipu.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 智普AI 配置类
 */
@Configuration
@ConfigurationProperties(prefix = "zhipu.ai")
@Data
public class ZhipuAiConfig {
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * 基础URL
     */
    private String baseUrl;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 模型类型 (zhipu/deepseek/tongyi)
     */
    private String modelType = "zhipu";
    
    /**
     * 是否使用模拟服务
     */
    private boolean useMock = false;
} 