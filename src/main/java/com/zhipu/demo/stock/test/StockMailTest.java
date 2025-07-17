package com.zhipu.demo.stock.test;

import com.zhipu.demo.stock.model.StockInfo;
import com.zhipu.demo.stock.model.StockPickResult;
import com.zhipu.demo.stock.service.StockMailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class StockMailTest {
    private static final Logger log = LoggerFactory.getLogger(StockMailTest.class);
    
    @Autowired
    private StockMailService stockMailService;
    
    public void testMailService() {
        log.info("开始测试邮件服务");
        
        try {
            // 创建测试数据
            List<StockPickResult> testResults = new ArrayList<>();
            testResults.add(new StockPickResult(
                new StockInfo("000001", "平安银行", false, false),
                "一阳穿三线+涨幅5.2%+量能放大+周线MACD红柱"
            ));
            testResults.add(new StockPickResult(
                new StockInfo("600036", "招商银行", false, false),
                "一阳穿三线+涨幅4.8%+量能放大+周线MACD红柱"
            ));
            
            // 测试发送选股结果邮件
            stockMailService.sendStockPickResult(testResults);
            log.info("选股结果邮件测试完成");
            
            // 测试发送风险提醒邮件
            stockMailService.sendRiskAlert("000001", "收益超过10%且15分钟MACD顶背离");
            log.info("风险提醒邮件测试完成");
            
        } catch (Exception e) {
            log.error("邮件服务测试失败", e);
        }
    }
} 