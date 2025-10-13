package com.zhipu.demo.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MapToJsonDemo {
    
    public static void main(String[] args) {
        // 创建一个Map对象
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 25);
        map.put("city", "北京");
        map.put("occupation", "软件工程师");
        
        // 添加一个嵌套的Map
        Map<String, String> contact = new HashMap<>();
        contact.put("email", "zhangsan@example.com");
        contact.put("phone", "13800138000");
        map.put("contact", contact);
        
        // 使用Jackson将Map转换为JSON并打印
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(map);
            log.info("Map转换为JSON的结果：");
            log.info(json);
        } catch (JsonProcessingException e) {
            log.error("将Map转换为JSON时出错：", e);
        }
    }
}