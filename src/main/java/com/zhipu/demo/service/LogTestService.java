package com.zhipu.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 日志测试服务
 */
@Slf4j
@Service
public class LogTestService {
    
    /**
     * 测试不同级别的日志
     */
    public void testLogging() {
        log.trace("这是一条TRACE级别的日志");
        log.debug("这是一条DEBUG级别的日志");
        log.info("这是一条INFO级别的日志");
        log.warn("这是一条WARN级别的日志");
        log.error("这是一条ERROR级别的日志");
        
        // 测试异常日志
        try {
            throw new RuntimeException("测试异常");
        } catch (Exception e) {
            log.error("捕获到异常", e);
        }
    }
    
    /**
     * 测试业务日志
     */
    public void testBusinessLog() {
        log.info("开始执行业务逻辑");
        log.debug("业务参数: {}", "测试参数");
        log.info("业务逻辑执行完成");
    }
} 