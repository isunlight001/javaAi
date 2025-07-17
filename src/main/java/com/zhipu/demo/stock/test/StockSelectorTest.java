package com.zhipu.demo.stock.test;

import com.zhipu.demo.stock.model.StockPickResult;
import com.zhipu.demo.stock.service.StockSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class StockSelectorTest {
    private static final Logger log = LoggerFactory.getLogger(StockSelectorTest.class);
    
    @Autowired
    private StockSelector stockSelector;
    
    public void testStockSelection() {
        log.info("开始测试选股功能");
        
        try {
            List<StockPickResult> results = stockSelector.selectStocks();
            
            log.info("选股测试完成，共选出 {} 只股票", results.size());
            
            if (results.isEmpty()) {
                log.info("没有选出符合条件的股票");
            } else {
                log.info("选股结果:");
                for (int i = 0; i < results.size(); i++) {
                    StockPickResult result = results.get(i);
                    log.info("{}. {}", i + 1, result.toString());
                }
            }
            
        } catch (Exception e) {
            log.error("选股测试失败", e);
        }
    }
} 