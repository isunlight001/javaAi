package com.zhipu.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * 智普AI 请求DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZhipuAiRequest {
    
    private String model;
    private List<Message> messages;
    private boolean stream = false;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
} 