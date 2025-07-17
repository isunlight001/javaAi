package com.zhipu.demo.controller;

import com.zhipu.demo.entity.User;
import com.zhipu.demo.service.LogTestService;
import com.zhipu.demo.service.UserService;
import com.zhipu.demo.service.ZhipuAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * 演示控制器 - 综合功能
 */
@RestController
@RequestMapping("/api/demo")
@Slf4j
public class DemoController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ZhipuAiService zhipuAiService;
    
    @Autowired
    private LogTestService logTestService;
    
    /**
     * 获取系统信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("application", "智普AI + MySQL 集成 Demo");
        info.put("version", "1.0.0");
        info.put("description", "Spring Boot 项目，集成智普AI和MySQL数据库");
        info.put("features", Arrays.asList(
            "MySQL数据库连接和操作",
            "智普AI API调用",
            "用户管理功能",
            "AI数据分析"
        ));
        info.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * 综合演示：获取用户数据并用AI分析
     */
    @GetMapping("/analyze-all-users")
    public ResponseEntity<Map<String, Object>> analyzeAllUsers() {
        log.info("开始分析所有用户数据");
        
        try {
            // 1. 获取所有用户
            List<User> users = userService.findAllUsers();
            
            // 2. 构建用户数据字符串
            StringBuilder userData = new StringBuilder();
            userData.append("用户总数: ").append(users.size()).append("\n\n");
            
            for (User user : users) {
                userData.append("用户ID: ").append(user.getId()).append("\n");
                userData.append("用户名: ").append(user.getUsername()).append("\n");
                userData.append("邮箱: ").append(user.getEmail()).append("\n");
                userData.append("描述: ").append(user.getDescription()).append("\n");
                userData.append("创建时间: ").append(user.getCreatedTime()).append("\n");
                userData.append("---\n");
            }
            
            // 3. 使用AI分析数据
            String analysis = zhipuAiService.analyzeUserData(userData.toString());
            
            // 4. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userCount", users.size());
            result.put("userData", userData.toString());
            result.put("aiAnalysis", analysis);
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
     * 快速测试：创建用户并分析
     */
    @PostMapping("/quick-test")
    public ResponseEntity<Map<String, Object>> quickTest(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String description = request.get("description");
        
        log.info("快速测试：创建用户 {}", username);
        
        try {
            // 1. 创建用户
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setDescription(description);
            
            User createdUser = userService.createUser(user);
            
            // 2. 构建用户信息
            String userInfo = String.format(
                "新创建用户信息：\n用户名: %s\n邮箱: %s\n描述: %s\n创建时间: %s",
                createdUser.getUsername(),
                createdUser.getEmail(),
                createdUser.getDescription(),
                createdUser.getCreatedTime()
            );
            
            // 3. AI分析
            String analysis = zhipuAiService.chat("请简要分析这个用户的特点：" + userInfo);
            
            // 4. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("user", createdUser);
            result.put("aiAnalysis", analysis);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("快速测试失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 数据库连接测试
     */
    @GetMapping("/db-test")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        log.info("测试数据库连接");
        
        try {
            long userCount = userService.countUsers();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "数据库连接成功");
            result.put("userCount", userCount);
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("数据库连接测试失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "数据库连接失败: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 测试日志功能
     */
    @GetMapping("/test-log")
    public ResponseEntity<Map<String, Object>> testLog() {
        log.info("开始测试日志功能");
        
        try {
            // 测试不同级别的日志
            logTestService.testLogging();
            
            // 测试业务日志
            logTestService.testBusinessLog();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "日志测试完成，请查看logs目录下的日志文件");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("日志测试失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
        }
    }
} 