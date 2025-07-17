package com.zhipu.demo.service;

import com.zhipu.demo.entity.User;
import com.zhipu.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 模拟AI服务 - 用于测试和备用
 */
@Service
@Slf4j
public class MockAiService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 模拟AI聊天
     */
    public String chat(String message) {
        log.info("模拟AI聊天，消息: {}", message);
        
        // 查询数据库信息
        String dbInfo = queryDatabaseInfo(message);
        
        if (message.contains("你好") || message.contains("介绍")) {
            return "你好！我是智普AI助手，很高兴为您服务。我可以帮助您回答问题、分析数据、编写代码等。\n\n" + dbInfo;
        } else if (message.contains("用户") || message.contains("人员")) {
            return "根据数据库信息，我为您提供以下用户相关回答：\n\n" + dbInfo + "\n\n您还需要了解什么具体信息吗？";
        } else if (message.contains("Java") || message.contains("开发")) {
            return "关于Java开发，我可以告诉您：\n\n" + dbInfo + "\n\nJava是一个优秀的编程语言，特别适合企业级应用开发。";
        } else if (message.contains("前端") || message.contains("Vue") || message.contains("React")) {
            return "关于前端开发，我为您查询到：\n\n" + dbInfo + "\n\n前端技术发展迅速，Vue和React都是优秀的选择。";
        } else if (message.contains("数据库") || message.contains("MySQL")) {
            return "关于数据库管理，我为您查询到：\n\n" + dbInfo + "\n\n数据库管理是系统架构中的重要组成部分。";
        } else {
            return "我理解您的问题：" + message + "\n\n基于数据库信息：\n" + dbInfo + "\n\n让我为您详细解答...";
        }
    }
    
    /**
     * 查询数据库信息
     */
    private String queryDatabaseInfo(String message) {
        try {
            StringBuilder info = new StringBuilder();
            long userCount = userRepository.countAllUsers();
            info.append("数据库中共有 ").append(userCount).append(" 个用户。\n");
            
            // 根据消息内容查询相关用户
            if (message.contains("Java") || message.contains("开发")) {
                List<User> developers = userRepository.findByDescriptionContaining("开发");
                if (!developers.isEmpty()) {
                    info.append("开发工程师：\n");
                    for (User dev : developers) {
                        info.append("- ").append(dev.getUsername()).append(": ").append(dev.getDescription()).append("\n");
                    }
                }
            }
            
            if (message.contains("前端")) {
                List<User> frontend = userRepository.findByDescriptionContaining("前端");
                if (!frontend.isEmpty()) {
                    info.append("前端工程师：\n");
                    for (User fe : frontend) {
                        info.append("- ").append(fe.getUsername()).append(": ").append(fe.getDescription()).append("\n");
                    }
                }
            }
            
            if (message.contains("数据库")) {
                List<User> dbUsers = userRepository.findByDescriptionContaining("数据库");
                if (!dbUsers.isEmpty()) {
                    info.append("数据库管理员：\n");
                    for (User db : dbUsers) {
                        info.append("- ").append(db.getUsername()).append(": ").append(db.getDescription()).append("\n");
                    }
                }
            }
            
            return info.toString();
        } catch (Exception e) {
            log.warn("查询数据库信息失败", e);
            return "数据库查询失败，但AI仍可回答问题。";
        }
    }
    
    /**
     * 模拟用户数据分析
     */
    public String analyzeUserData(String userData) {
        log.info("模拟用户数据分析，数据: {}", userData);
        
        return "用户数据分析报告：\n\n" +
               "1. 数据概览：共分析了 " + userData.split("\n").length + " 条用户记录\n" +
               "2. 用户类型分布：包含开发工程师、前端工程师、数据库管理员等\n" +
               "3. 技能分布：Java开发、前端技术、数据库管理等\n" +
               "4. 建议：可以考虑根据用户技能进行分组管理，提供个性化服务\n\n" +
               "这是基于您提供数据的初步分析结果。";
    }
} 