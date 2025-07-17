package com.zhipu.demo.stock.service;

import com.zhipu.demo.stock.model.StockInfo;
import java.util.List;

public interface StockDataService {
    List<StockInfo> getAllStocks();
    boolean isYangChuanSanXian(StockInfo stock);
    double getTodayChangePercent(StockInfo stock);
    boolean isVolumeUp(StockInfo stock);
    boolean isWeekMACDRed(StockInfo stock);
    double getCurrentPrice(String code);
    boolean isMACDTopDivergence15Min(String code);
} 