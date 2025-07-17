package com.zhipu.demo.ouyi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ouyi")
public class OuyiController {
    @Autowired
    private OuyiService ouyiService;

    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody Map<String, String> body) {
        String symbol = body.get("symbol");
        String leverage = body.get("leverage");
        Map<String, Object> result = new HashMap<>();
        try {
            ouyiService.startStrategy(symbol, Integer.parseInt(leverage));
            result.put("success", true);
            result.put("message", "策略已启动，正在监听MACD信号");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
} 