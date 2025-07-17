package com.zhipu.demo.stock.controller;

import com.zhipu.demo.stock.model.StockPickResult;
import com.zhipu.demo.stock.service.StockSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockSelectorController {
    @Autowired
    private StockSelector stockSelector;

    /**
     * 一键选股接口，供前端调用
     */
    @GetMapping("/select")
    public List<StockPickResult> selectStocks() {
        return stockSelector.selectStocks();
    }
} 