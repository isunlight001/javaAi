package com.zhipu.demo.stockAi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stockAi")
public class StockAiController {
    @Autowired
    private StockAiService stockAiService;

    @PostMapping("/analyze")
    public Map<String, Object> analyze(@RequestBody Map<String, String> body) {
        String stockCode = body.get("stockCode");
        String modelType = body.get("modelType");
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> analysisResult = stockAiService.analyzeStockWithTokenInfo(stockCode, modelType);
            result.put("suggestion", analysisResult.get("suggestion"));
            result.put("tokenInfo", analysisResult.get("tokenInfo"));
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }
} 