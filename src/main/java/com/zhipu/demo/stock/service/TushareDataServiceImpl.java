package com.zhipu.demo.stock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class TushareDataServiceImpl implements TushareDataService {

    @Value("${tushare.token}")
    private String tushareToken;

    private static final String TUSHARE_URL = "http://api.tushare.pro";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> query(String apiName, Map<String, Object> params, String fields) {
        Map<String, Object> body = new HashMap<>();
        body.put("api_name", apiName);
        body.put("token", tushareToken);
        body.put("params", params);
        body.put("fields", fields);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map<String, Object> result = restTemplate.postForObject(TUSHARE_URL, request, Map.class);
        return result;
    }
} 