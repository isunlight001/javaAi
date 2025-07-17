package com.zhipu.demo.bugfix.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.zhipu.demo.config.ZhipuAiConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * AI模型工厂类 - 使用工厂模式支持多种大模型
 */
@Component
@Slf4j
public class AiModelFactory {
    
    @Autowired
    private ZhipuAiConfig zhipuAiConfig;
    
    @Autowired
    @Qualifier("bugfixZhipuAiService")
    private ZhipuAiService zhipuAiService;
    
    @Autowired
    @Qualifier("deepSeekAiService")
    private DeepSeekAiService deepSeekAiService;
    
    @Autowired
    @Qualifier("tongyiAiService")
    private TongyiAiService tongyiAiService;
    
    /**
     * 根据配置获取AI模型服务
     */
    public AiModelService getAiModelService() {
        String modelType = zhipuAiConfig.getModelType();
        
        switch (modelType.toLowerCase()) {
            case "zhipu":
            case "glm":
            case "glm-4":
                log.info("使用智谱AI模型: {}", modelType);
                return zhipuAiService;
                
            case "deepseek":
            case "deepseek-chat":
            case "deepseek-coder":
                log.info("使用DeepSeek模型: {}", modelType);
                return deepSeekAiService;
                
            case "tongyi":
            case "qwen":
            case "qwen-turbo":
            case "qwen-plus":
                log.info("使用通义千问模型: {}", modelType);
                return tongyiAiService;
                
            default:
                log.warn("未知的模型类型: {}，默认使用智谱AI", modelType);
                return zhipuAiService;
        }
    }
    
    /**
     * 根据模型类型获取AI模型服务
     */
    public AiModelService getAiModelService(String modelType) {
        switch (modelType.toLowerCase()) {
            case "zhipu":
            case "glm":
            case "glm-4":
                return zhipuAiService;
                
            case "deepseek":
            case "deepseek-chat":
            case "deepseek-coder":
                return deepSeekAiService;
                
            case "tongyi":
            case "qwen":
            case "qwen-turbo":
            case "qwen-plus":
                return tongyiAiService;
                
            default:
                log.warn("未知的模型类型: {}，默认使用智谱AI", modelType);
                return zhipuAiService;
        }
    }
    
    /**
     * 获取所有支持的模型类型
     */
    public String[] getSupportedModels() {
        return new String[]{
            "zhipu", "glm", "glm-4",
            "deepseek", "deepseek-chat", "deepseek-coder", 
            "tongyi", "qwen", "qwen-turbo", "qwen-plus"
        };
    }
} 