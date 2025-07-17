package com.zhipu.demo.stock.model;

public class StockPosition {
    private String code;
    private double buyPrice;
    private double buyDayLow;

    public StockPosition(String code, double buyPrice, double buyDayLow) {
        this.code = code;
        this.buyPrice = buyPrice;
        this.buyDayLow = buyDayLow;
    }

    public String getCode() { return code; }
    public double getBuyPrice() { return buyPrice; }
    public double getBuyDayLow() { return buyDayLow; }
} 