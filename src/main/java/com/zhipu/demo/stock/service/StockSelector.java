package com.zhipu.demo.stock.service;

import com.zhipu.demo.stock.model.StockInfo;
import com.zhipu.demo.stock.model.StockPickResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockSelector {
    private static final Logger log = LoggerFactory.getLogger(StockSelector.class);
    
    @Autowired
    private StockDataService stockDataService;
    
    /**
     * 原有选股逻辑
     */
    public List<StockPickResult> selectStocks() {
        log.info("开始执行选股逻辑");
        List<StockPickResult> results = new ArrayList<>();
        List<StockInfo> allStocks = stockDataService.getAllStocks();
        log.info("获取到股票总数: {}", allStocks.size());
        for (StockInfo stock : allStocks) {
            log.debug("检查股票: {} ({})", stock.getName(), stock.getCode());
            // 1. 剔除ST股和风险警示股
            if (stock.isST() || stock.isRisk()) {
                log.debug("股票: {} 被剔除 - ST: {}, 风险警示: {}", 
                        stock.getCode(), stock.isST(), stock.isRisk());
                continue;
            }
            // 2. 检查一阳穿三线
            if (!stockDataService.isYangChuanSanXian(stock)) {
                log.debug("股票: {} 不满足一阳穿三线条件", stock.getCode());
                continue;
            }
            // 3. 检查涨幅大于3%
            double changePercent = stockDataService.getTodayChangePercent(stock);
            if (changePercent <= 3.0) {
                log.debug("股票: {} 涨幅不足3%，当前涨幅: {}%", stock.getCode(), changePercent);
                continue;
            }
            // 4. 检查量能放大
            if (!stockDataService.isVolumeUp(stock)) {
                log.debug("股票: {} 量能未放大", stock.getCode());
                continue;
            }
            // 5. 检查周线MACD红柱
            if (!stockDataService.isWeekMACDRed(stock)) {
                log.debug("股票: {} 周线MACD非红柱", stock.getCode());
                continue;
            }
            // 所有条件都满足，加入选股结果
            String reason = String.format("一阳穿三线+涨幅%.1f%%+量能放大+周线MACD红柱", changePercent);
            StockPickResult result = new StockPickResult(stock, reason);
            results.add(result);
            log.info("股票: {} ({}) 符合选股条件，加入结果", stock.getName(), stock.getCode());
        }
        log.info("选股完成，共选出 {} 只股票", results.size());
        return results;
    }

    /**
     * 一键选股逻辑，直接返回选股结果
     */
    public List<StockPickResult> selectStocksOneClick() {
        log.info("进入一键选股逻辑 selectStocksOneClick");
        // 可根据实际需求调整逻辑，这里直接复用selectStocks
        return selectStocks();
    }
} 