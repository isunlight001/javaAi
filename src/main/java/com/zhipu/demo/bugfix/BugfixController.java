package com.zhipu.demo.bugfix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/bugfix")
public class BugfixController {
    @Autowired
    private BugfixService bugfixService;

    @PostMapping("/analyze")
    public Map<String, Object> analyze(@RequestBody Map<String, String> body) {
        String serialNo = body.get("serialNo");
        String modelType = body.get("modelType");
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> analysisResult = bugfixService.analyzeLogWithTokenInfo(serialNo, modelType);
            result.put("suggestion", analysisResult.get("suggestion"));
            result.put("tokenInfo", analysisResult.get("tokenInfo"));
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @PostMapping("/analyze-with-memory")
    public Map<String, Object> analyzeWithMemory(@RequestBody Map<String, String> body) {
        String serialNo = body.get("serialNo");
        String sessionId = body.get("sessionId");
        String modelType = body.get("modelType");
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> analysisResult = bugfixService.analyzeLogWithMemory(serialNo, sessionId, modelType);
            result.put("suggestion", analysisResult.get("suggestion"));
            result.put("tokenInfo", analysisResult.get("tokenInfo"));
            result.put("sessionId", analysisResult.get("sessionId"));
            result.put("messageCount", analysisResult.get("messageCount"));
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/session/{sessionId}")
    public Map<String, Object> getSessionInfo(@PathVariable String sessionId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> sessionInfo = bugfixService.getSessionInfo(sessionId);
            if (sessionInfo != null) {
                result.put("success", true);
                result.put("sessionInfo", sessionInfo);
            } else {
                result.put("success", false);
                result.put("error", "会话不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @DeleteMapping("/session/{sessionId}")
    public Map<String, Object> clearSession(@PathVariable String sessionId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean cleared = bugfixService.clearSession(sessionId);
            result.put("success", cleared);
            if (cleared) {
                result.put("message", "会话已清除");
            } else {
                result.put("message", "会话不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/sessions")
    public Map<String, Object> getAllSessions() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> sessions = bugfixService.getAllSessions();
            result.put("success", true);
            result.put("sessions", sessions);
            result.put("count", sessions.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/models")
    public Map<String, Object> getSupportedModels() {
        Map<String, Object> result = new HashMap<>();
        try {
            String[] models = bugfixService.getSupportedModels();
            result.put("success", true);
            result.put("models", models);
            result.put("count", models.length);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
} 