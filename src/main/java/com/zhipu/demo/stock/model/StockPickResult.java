package com.zhipu.demo.stock.model;

public class StockPickResult {
    private StockInfo stock;
    private String reason;

    public StockPickResult(StockInfo stock, String reason) {
        this.stock = stock;
        this.reason = reason;
    }

    public StockInfo getStock() { return stock; }
    public String getReason() { return reason; }
    
    @Override
    public String toString() {
        return "股票代码: " + stock.getCode() + 
               ", 股票名称: " + stock.getName() + 
               ", 选股理由: " + reason;
    }
} 