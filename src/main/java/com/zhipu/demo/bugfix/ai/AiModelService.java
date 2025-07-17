package com.zhipu.demo.bugfix.ai;

/**
 * AI模型服务接口 - 定义统一的AI调用方法
 */
public interface AiModelService {
    
    /**
     * 调用AI模型进行对话
     * @param prompt 输入提示词
     * @return AI响应内容
     * @throws Exception 调用异常
     */
    String chat(String prompt) throws Exception;
    
    /**
     * 获取模型名称
     * @return 模型名称
     */
    String getModelName();
    
    /**
     * 获取模型类型
     * @return 模型类型
     */
    String getModelType();
    
    /**
     * 检查模型是否可用
     * @return 是否可用
     */
    boolean isAvailable();
} 