package com.zhipu.demo.stock.service;

import com.zhipu.demo.stock.model.StockPickResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StockScheduler {
    private static final Logger log = LoggerFactory.getLogger(StockScheduler.class);
    
    @Autowired
    private StockSelector stockSelector;
    
    @Autowired
    private StockMailService stockMailService;
    
    @Scheduled(cron = "0 30 14 * * ?") // 每天14:30执行
    public void morningStockPick() {
        log.info("开始执行上午选股任务");
        try {
            List<StockPickResult> results = stockSelector.selectStocks();
            stockMailService.sendStockPickResult(results);
            log.info("上午选股任务完成，选出股票数量: {}", results.size());
        } catch (Exception e) {
            log.error("上午选股任务执行失败", e);
        }
    }
    
    @Scheduled(cron = "0 50 14 * * ?") // 每天14:50执行
    public void afternoonStockPick() {
        log.info("开始执行下午选股任务");
        try {
            List<StockPickResult> results = stockSelector.selectStocks();
            stockMailService.sendStockPickResult(results);
            log.info("下午选股任务完成，选出股票数量: {}", results.size());
        } catch (Exception e) {
            log.error("下午选股任务执行失败", e);
        }
    }
} 