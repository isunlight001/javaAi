package com.zhipu.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhipu.demo.config.ZhipuAiConfig;
import com.zhipu.demo.dto.ZhipuAiRequest;
import com.zhipu.demo.dto.ZhipuAiResponse;
import com.zhipu.demo.entity.User;
import com.zhipu.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.List;

/**
 * 智普AI 服务类
 */
@Service
@Primary
@Slf4j
public class ZhipuAiService {
    
    @Autowired
    private ZhipuAiConfig zhipuAiConfig;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MockAiService mockAiService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 调用智普AI 聊天接口（带数据库查询）
     */
    public String chat(String message) {
        // 如果配置为使用模拟服务，直接使用模拟服务
        if (zhipuAiConfig.isUseMock()) {
            log.info("使用模拟AI服务");
            return mockAiService.chat(message);
        }
        
        try {
            // 1. 查询本地数据库信息
            String dbContext = queryDatabaseContext(message);
            
            // 2. 构建增强的提示词
            String enhancedMessage = buildEnhancedMessage(message, dbContext);
            
            // 3. 构建请求
            ZhipuAiRequest request = new ZhipuAiRequest();
            request.setModel(zhipuAiConfig.getModel());
            request.setMessages(Arrays.asList(
                new ZhipuAiRequest.Message("user", enhancedMessage)
            ));
            
            // 4. 发送请求
            String response = sendRequest(request);
            
            // 5. 解析响应
            ZhipuAiResponse aiResponse = objectMapper.readValue(response, ZhipuAiResponse.class);
            
            if (aiResponse.getChoices() != null && !aiResponse.getChoices().isEmpty()) {
                ZhipuAiResponse.Message messageObj = aiResponse.getChoices().get(0).getMessage();
                if (messageObj != null && messageObj.getContent() != null) {
                    return messageObj.getContent();
                }
            }
            
            // 如果没有有效回复，返回原始响应用于调试
            log.warn("AI响应格式异常，原始响应: {}", response);
            return "抱歉，AI 没有返回有效回复，请检查API配置";
            
        } catch (Exception e) {
            log.error("调用智普AI 失败，使用模拟服务", e);
            log.info("使用模拟AI服务");
            return mockAiService.chat(message);
        }
    }
    
    /**
     * 查询数据库上下文信息
     */
    private String queryDatabaseContext(String message) {
        StringBuilder context = new StringBuilder();
        
        try {
            // 查询用户总数
            long userCount = userRepository.countAllUsers();
            context.append("数据库用户总数: ").append(userCount).append("\n");
            
            // 根据消息内容智能查询相关数据
            if (message.contains("用户") || message.contains("人员") || message.contains("人员")) {
                List<User> users = userRepository.findAll();
                if (!users.isEmpty()) {
                    context.append("用户列表:\n");
                    for (User user : users) {
                        context.append("- ").append(user.getUsername())
                               .append(" (").append(user.getEmail()).append(")")
                               .append(": ").append(user.getDescription())
                               .append("\n");
                    }
                }
            }
            
            // 如果消息包含特定用户名，查询该用户信息
            for (User user : userRepository.findAll()) {
                if (message.contains(user.getUsername())) {
                    context.append("找到相关用户信息:\n");
                    context.append("用户名: ").append(user.getUsername()).append("\n");
                    context.append("邮箱: ").append(user.getEmail()).append("\n");
                    context.append("描述: ").append(user.getDescription()).append("\n");
                    context.append("创建时间: ").append(user.getCreatedTime()).append("\n");
                    break;
                }
            }
            
            // 如果消息包含技能关键词，查询相关用户
            if (message.contains("Java") || message.contains("开发") || message.contains("工程师")) {
                List<User> developers = userRepository.findByDescriptionContaining("开发");
                if (!developers.isEmpty()) {
                    context.append("开发工程师:\n");
                    for (User dev : developers) {
                        context.append("- ").append(dev.getUsername())
                               .append(": ").append(dev.getDescription()).append("\n");
                    }
                }
            }
            
            if (message.contains("前端") || message.contains("Vue") || message.contains("React")) {
                List<User> frontend = userRepository.findByDescriptionContaining("前端");
                if (!frontend.isEmpty()) {
                    context.append("前端工程师:\n");
                    for (User fe : frontend) {
                        context.append("- ").append(fe.getUsername())
                               .append(": ").append(fe.getDescription()).append("\n");
                    }
                }
            }
            
            if (message.contains("数据库") || message.contains("MySQL") || message.contains("Oracle")) {
                List<User> dbUsers = userRepository.findByDescriptionContaining("数据库");
                if (!dbUsers.isEmpty()) {
                    context.append("数据库管理员:\n");
                    for (User db : dbUsers) {
                        context.append("- ").append(db.getUsername())
                               .append(": ").append(db.getDescription()).append("\n");
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("查询数据库上下文失败", e);
            context.append("数据库查询失败，但AI仍可回答问题。\n");
        }
        
        return context.toString();
    }
    
    /**
     * 构建增强的提示词
     */
    private String buildEnhancedMessage(String originalMessage, String dbContext) {
        StringBuilder enhanced = new StringBuilder();
        
        if (!dbContext.trim().isEmpty()) {
            enhanced.append("基于以下数据库信息回答问题：\n\n");
            enhanced.append(dbContext).append("\n");
            enhanced.append("用户问题: ").append(originalMessage).append("\n\n");
            enhanced.append("请根据上述数据库信息，结合你的知识，为用户提供准确、有用的回答。");
        } else {
            enhanced.append(originalMessage);
        }
        
        return enhanced.toString();
    }
    
    /**
     * 发送HTTP请求到智普AI
     */
    private String sendRequest(ZhipuAiRequest request) throws Exception {
        // 尝试不同的API端点
        String[] endpoints = {
            "/chat/completions",
            "/v4/chat/completions",
            "/v3/chat/completions"
        };
        
        Exception lastException = null;
        
        for (String endpoint : endpoints) {
            try {
                String result = tryEndpoint(zhipuAiConfig.getBaseUrl() + endpoint, request);
                log.info("成功使用API端点: {}", endpoint);
                return result;
            } catch (Exception e) {
                log.warn("API端点 {} 失败: {}", endpoint, e.getMessage());
                lastException = e;
            }
        }
        
        // 所有端点都失败了
        throw new RuntimeException("所有API端点都失败，最后一个错误: " + lastException.getMessage());
    }
    
    /**
     * 尝试特定的API端点
     */
    private String tryEndpoint(String url, ZhipuAiRequest request) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + zhipuAiConfig.getApiKey());
            httpPost.setHeader("Accept", "application/json");
            
            // 设置请求体
            String requestBody = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));
            
            log.info("尝试API端点: {}", url);
            log.debug("请求体: {}", requestBody);
            
            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity, "UTF-8");
                
                log.info("API端点 {} 响应状态码: {}", url, response.getStatusLine().getStatusCode());
                log.debug("API端点 {} 响应内容: {}", url, responseBody);
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("API调用失败，状态码: " + response.getStatusLine().getStatusCode() + ", 响应: " + responseBody);
                }
                
                return responseBody;
            }
        } catch (Exception e) {
            log.error("API端点 {} 请求失败", url, e);
            throw e;
        }
    }
    
    /**
     * 分析用户数据并生成报告
     */
    public String analyzeUserData(String userData) {
        try {
            String prompt = String.format(
                "请分析以下用户数据并生成简要报告：\n%s\n\n请用中文回复，格式要清晰。",
                userData
            );
            return chat(prompt);
        } catch (Exception e) {
            log.error("分析用户数据失败，使用模拟服务", e);
            log.info("使用模拟AI服务分析用户数据");
            return mockAiService.analyzeUserData(userData);
        }
    }
    
    /**
     * 检查是否使用模拟服务
     */
    public boolean isUsingMock() {
        return zhipuAiConfig.isUseMock();
    }
} 