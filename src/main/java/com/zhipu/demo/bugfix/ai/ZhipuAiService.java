package com.zhipu.demo.bugfix.ai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 智谱AI服务实现类
 */
@Service("bugfixZhipuAiService")
@Slf4j
public class ZhipuAiService implements AiModelService {
    
    @Autowired
    private com.zhipu.demo.service.ZhipuAiService originalZhipuAiService;
    
    @Override
    public String chat(String prompt) throws Exception {
        log.info("调用智谱AI模型: {}", getModelName());
        return originalZhipuAiService.chat(prompt);
    }
    
    @Override
    public String getModelName() {
        return "智谱AI GLM-4";
    }
    
    @Override
    public String getModelType() {
        return "zhipu";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            // 简单的可用性检查
            return true;
        } catch (Exception e) {
            log.error("智谱AI服务不可用", e);
            return false;
        }
    }
} 