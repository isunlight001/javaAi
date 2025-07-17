package com.zhipu.demo.controller;

import com.zhipu.demo.service.ZhipuAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 控制器
 */
@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AiController {
    
    @Autowired
    private ZhipuAiService zhipuAiService;
    
    /**
     * 聊天接口
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("AI聊天请求: {}", message);
        
        try {
            String response = zhipuAiService.chat(message);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("AI聊天失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 分析用户数据
     */
    @PostMapping("/analyze-users")
    public ResponseEntity<Map<String, Object>> analyzeUsers(@RequestBody String userData) {
        log.info("分析用户数据请求");
        
        try {
            String analysis = zhipuAiService.analyzeUserData(userData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("analysis", analysis);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("分析用户数据失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 智能查询（带数据库上下文）
     */
    @PostMapping("/smart-query")
    public ResponseEntity<Map<String, Object>> smartQuery(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        log.info("智能查询请求: {}", message);
        
        try {
            String response = zhipuAiService.chat(message);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response);
            result.put("useMock", zhipuAiService.isUsingMock());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("智能查询失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "智普AI服务");
        result.put("useMock", zhipuAiService.isUsingMock());
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }
} 