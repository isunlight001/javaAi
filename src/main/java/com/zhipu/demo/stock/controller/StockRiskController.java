package com.zhipu.demo.stock.controller;

import com.zhipu.demo.stock.model.StockPosition;
import com.zhipu.demo.stock.service.StockDataService;
import com.zhipu.demo.stock.service.StockMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stock/risk")
public class StockRiskController {
    private static final Logger log = LoggerFactory.getLogger(StockRiskController.class);
    
    @Autowired
    private StockDataService stockDataService;
    
    @Autowired
    private StockMailService stockMailService;
    
    // 模拟持仓数据
    private final Map<String, StockPosition> positions = new HashMap<>();
    
    @PostMapping("/check")
    public Map<String, Object> checkRisk(@RequestBody StockPosition position) {
        log.info("检查股票风险，股票代码: {}", position.getCode());
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", position.getCode());
        result.put("checkTime", java.time.LocalDateTime.now());
        
        try {
            double currentPrice = stockDataService.getCurrentPrice(position.getCode());
            double buyPrice = position.getBuyPrice();
            double buyDayLow = position.getBuyDayLow();
            
            // 计算收益率
            double profitPercent = (currentPrice - buyPrice) / buyPrice * 100;
            
            // 止盈检查：收益超过10%且15分钟MACD顶背离
            if (profitPercent > 10 && stockDataService.isMACDTopDivergence15Min(position.getCode())) {
                String reason = String.format("收益超过10%%且15分钟MACD顶背离，当前收益: %.2f%%", profitPercent);
                stockMailService.sendRiskAlert(position.getCode(), reason);
                result.put("action", "止盈");
                result.put("reason", reason);
                log.info("股票: {} 触发止盈条件，收益: {}%", position.getCode(), profitPercent);
            }
            // 止损检查：收盘价低于买入日最低价
            else if (currentPrice < buyDayLow) {
                String reason = String.format("收盘价低于买入日最低价，当前价: %.2f, 买入日最低价: %.2f", currentPrice, buyDayLow);
                stockMailService.sendRiskAlert(position.getCode(), reason);
                result.put("action", "止损");
                result.put("reason", reason);
                log.info("股票: {} 触发止损条件，当前价: {}, 买入日最低价: {}", position.getCode(), currentPrice, buyDayLow);
            }
            else {
                result.put("action", "持有");
                result.put("reason", "未触发止盈止损条件");
                log.debug("股票: {} 未触发风控条件，当前收益: {}%", position.getCode(), profitPercent);
            }
            
            result.put("currentPrice", currentPrice);
            result.put("profitPercent", profitPercent);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("检查股票风险失败，股票代码: {}", position.getCode(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    @PostMapping("/position")
    public Map<String, Object> addPosition(@RequestBody StockPosition position) {
        log.info("添加持仓，股票代码: {}", position.getCode());
        
        positions.put(position.getCode(), position);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "持仓添加成功");
        result.put("code", position.getCode());
        
        return result;
    }
    
    @GetMapping("/positions")
    public Map<String, Object> getPositions() {
        log.info("获取所有持仓");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("positions", positions.values());
        result.put("count", positions.size());
        
        return result;
    }
} 