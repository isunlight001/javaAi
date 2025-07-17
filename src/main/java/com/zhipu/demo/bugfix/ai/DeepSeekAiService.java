package com.zhipu.demo.bugfix.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * DeepSeek AI服务实现类
 */
@Service("deepSeekAiService")
@Slf4j
public class DeepSeekAiService implements AiModelService {
    
    @Value("${deepseek.api-key:}")
    private String apiKey;
    
    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;
    
    @Value("${deepseek.model:deepseek-chat}")
    private String model;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String chat(String prompt) throws Exception {
        log.info("调用DeepSeek AI模型: {}", getModelName());
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("DeepSeek API Key未配置");
        }
        
        // 构建请求
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        request.put("messages", Arrays.asList(userMessage));
        request.put("temperature", 0.7);
        request.put("max_tokens", 4000);
        
        // 发送请求
        String response = sendRequest(request);
        
        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            if (message != null) {
                return (String) message.get("content");
            }
        }
        
        throw new RuntimeException("DeepSeek API响应格式异常: " + response);
    }
    
    private String sendRequest(Map<String, Object> request) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(baseUrl + "/v1/chat/completions");
            
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);
            
            // 设置请求体
            String requestBody = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(requestBody, "UTF-8"));
            
            log.debug("DeepSeek请求体: {}", requestBody);
            
            // 发送请求并获取响应
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity, "UTF-8");
                
                log.debug("DeepSeek响应状态码: {}", response.getStatusLine().getStatusCode());
                log.debug("DeepSeek响应内容: {}", responseBody);
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new RuntimeException("DeepSeek API调用失败，状态码: " + response.getStatusLine().getStatusCode() + ", 响应: " + responseBody);
                }
                
                return responseBody;
            }
        } catch (Exception e) {
            log.error("DeepSeek API请求失败", e);
            throw e;
        }
    }
    
    @Override
    public String getModelName() {
        return "DeepSeek " + model;
    }
    
    @Override
    public String getModelType() {
        return "deepseek";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            return apiKey != null && !apiKey.trim().isEmpty();
        } catch (Exception e) {
            log.error("DeepSeek AI服务不可用", e);
            return false;
        }
    }
} 