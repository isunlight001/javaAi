package com.zhipu.demo.stock.model;

public class StockInfo {
    private String code;
    private String name;
    private boolean st;
    private boolean risk;
    private String area;
    private String industry;
    private String listDate;

    public StockInfo(String code, String name, boolean st, boolean risk) {
        this.code = code;
        this.name = name;
        this.st = st;
        this.risk = risk;
    }

    public StockInfo(String code, String name, boolean st, boolean risk, String area, String industry, String listDate) {
        this.code = code;
        this.name = name;
        this.st = st;
        this.risk = risk;
        this.area = area;
        this.industry = industry;
        this.listDate = listDate;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isST() { return st; }
    public boolean isRisk() { return risk; }
    public String getArea() { return area; }
    public String getIndustry() { return industry; }
    public String getListDate() { return listDate; }

    @Override
    public String toString() {
        return "股票代码: " + code + ", 股票名称: " + name + ", 地区: " + area + ", 行业: " + industry + ", 上市日期: " + listDate;
    }
} 