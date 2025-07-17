package com.zhipu.demo.stock.service;

import com.zhipu.demo.stock.model.StockInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Primary
@Service
public class StockDataServiceImpl implements StockDataService {
    private static final Logger log = LoggerFactory.getLogger(StockDataServiceImpl.class);

    @Autowired
    private TushareDataService tushareDataService;

    /**
     * 获取全部A股股票列表，基于tushare真实数据
     * @return 股票信息列表
     */
    @Override
    public List<StockInfo> getAllStocks() {
        log.info("进入 getAllStocks 方法，无参数");
        List<StockInfo> stocks = new ArrayList<>();
        try {
            Map<String, Object> params = new HashMap<>();
            // 只查上市股票，可扩展exchange参数
            params.put("list_status", "L");
            String fields = "ts_code,symbol,name,area,industry,list_date";
            Map<String, Object> result = tushareDataService.query("stock_basic", params, fields);
            // 解析tushare返回的数据结构
            if (result != null && result.get("code") != null && ((Integer)result.get("code")) == 0) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                List<String> fieldList = (List<String>) data.get("fields");
                List<List<Object>> items = (List<List<Object>>) data.get("items");
                int codeIdx = fieldList.indexOf("ts_code");
                int nameIdx = fieldList.indexOf("name");
                int areaIdx = fieldList.indexOf("area");
                int industryIdx = fieldList.indexOf("industry");
                int listDateIdx = fieldList.indexOf("list_date");
                for (List<Object> item : items) {
                    String code = (String) item.get(codeIdx);
                    String name = (String) item.get(nameIdx);
                    String area = areaIdx >= 0 ? (String) item.get(areaIdx) : "";
                    String industry = industryIdx >= 0 ? (String) item.get(industryIdx) : "";
                    String listDate = listDateIdx >= 0 ? (String) item.get(listDateIdx) : "";
                    stocks.add(new StockInfo(code, name, false, false, area, industry, listDate));
                }
            } else {
                log.warn("tushare返回异常: {}", result);
            }
        } catch (Exception e) {
            log.error("调用tushare获取股票列表失败", e);
        }
        log.info("获取到股票数量: {}", stocks.size());
        return stocks;
    }

    /**
     * 判断是否一阳穿三线（收盘价上穿5/10/20日均线），基于tushare数据
     */
    @Override
    public boolean isYangChuanSanXian(StockInfo stock) {
        log.info("进入 isYangChuanSanXian 方法，参数: {}", stock);
        try {
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            // 1. 获取最近1日的收盘价
            Map<String, Object> params = new HashMap<>();
            params.put("ts_code", stock.getCode());
            params.put("start_date", today);
            params.put("end_date", today);
            Map<String, Object> dailyResult = tushareDataService.query("daily", params, "close");
            double close = 0;
            // 解析收盘价
            if (dailyResult != null && dailyResult.get("code") != null && ((Integer)dailyResult.get("code")) == 0) {
                Map<String, Object> data = (Map<String, Object>) dailyResult.get("data");
                List<String> fieldList = (List<String>) data.get("fields");
                List<List<Object>> items = (List<List<Object>>) data.get("items");
                if (!items.isEmpty()) {
                    int closeIdx = fieldList.indexOf("close");
                    close = ((Number) items.get(0).get(closeIdx)).doubleValue();
                }
            }
            // 2. 获取5/10/20日均线
            params.clear();
            params.put("ts_code", stock.getCode());
            params.put("start_date", today);
            params.put("end_date", today);
            params.put("ma", "5,10,20");
            Map<String, Object> maResult = tushareDataService.query("ma", params, "ma5,ma10,ma20");
            double ma5 = 0, ma10 = 0, ma20 = 0;
            // 解析均线数据
            if (maResult != null && maResult.get("code") != null && ((Integer)maResult.get("code")) == 0) {
                Map<String, Object> data = (Map<String, Object>) maResult.get("data");
                List<String> fieldList = (List<String>) data.get("fields");
                List<List<Object>> items = (List<List<Object>>) data.get("items");
                if (!items.isEmpty()) {
                    int ma5Idx = fieldList.indexOf("ma5");
                    int ma10Idx = fieldList.indexOf("ma10");
                    int ma20Idx = fieldList.indexOf("ma20");
                    ma5 = ((Number) items.get(0).get(ma5Idx)).doubleValue();
                    ma10 = ((Number) items.get(0).get(ma10Idx)).doubleValue();
                    ma20 = ((Number) items.get(0).get(ma20Idx)).doubleValue();
                }
            }
            // 判断一阳穿三线
            boolean result = close > ma5 && close > ma10 && close > ma20;
            log.debug("股票: {} 一阳穿三线检查 - 当前价: {}, MA5: {}, MA10: {}, MA20: {}, 结果: {}", 
                    stock.getCode(), close, ma5, ma10, ma20, result);
            return result;
        } catch (Exception e) {
            log.error("调用tushare判断一阳穿三线失败", e);
            return false;
        }
    }

    /**
     * 获取当日涨跌幅，基于tushare数据
     */
    @Override
    public double getTodayChangePercent(StockInfo stock) {
        log.info("进入 getTodayChangePercent 方法，参数: {}", stock);
        try {
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            Map<String, Object> params = new HashMap<>();
            params.put("ts_code", stock.getCode());
            params.put("start_date", today);
            params.put("end_date", today);
            Map<String, Object> dailyResult = tushareDataService.query("daily", params, "pct_chg");
            // 解析涨跌幅
            if (dailyResult != null && dailyResult.get("code") != null && ((Integer)dailyResult.get("code")) == 0) {
                Map<String, Object> data = (Map<String, Object>) dailyResult.get("data");
                List<String> fieldList = (List<String>) data.get("fields");
                List<List<Object>> items = (List<List<Object>>) data.get("items");
                if (!items.isEmpty()) {
                    int idx = fieldList.indexOf("pct_chg");
                    return ((Number) items.get(0).get(idx)).doubleValue();
                }
            }
        } catch (Exception e) {
            log.error("调用tushare获取涨跌幅失败", e);
        }
        return 0;
    }

    /**
     * 判断量能是否放大（当前成交量大于5日均量1.2倍），基于tushare数据
     */
    @Override
    public boolean isVolumeUp(StockInfo stock) {
        log.info("进入 isVolumeUp 方法，参数: {}", stock);
        try {
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            Map<String, Object> params = new HashMap<>();
            params.put("ts_code", stock.getCode());
            params.put("start_date", today);
            params.put("end_date", today);
            Map<String, Object> dailyResult = tushareDataService.query("daily", params, "vol");
            double todayVol = 0;
            // 解析今日成交量
            if (dailyResult != null && dailyResult.get("code") != null && ((Integer)dailyResult.get("code")) == 0) {
                Map<String, Object> data = (Map<String, Object>) dailyResult.get("data");
                List<String> fieldList = (List<String>) data.get("fields");
                List<List<Object>> items = (List<List<Object>>) data.get("items");
                if (!items.isEmpty()) {
                    int idx = fieldList.indexOf("vol");
                    todayVol = ((Number) items.get(0).get(idx)).doubleValue();
                }
            }
            // 获取近5日均量
            params.clear();
            params.put("ts_code", stock.getCode());
            params.put("start_date", LocalDate.now().minusDays(5).format(DateTimeFormatter.BASIC_ISO_DATE));
            params.put("end_date", today);
            Map<String, Object> volResult = tushareDataService.query("daily", params, "vol");
            double avgVol = 0;
            // 解析5日均量
            if (volResult != null && volResult.get("code") != null && ((Integer)volResult.get("code")) == 0) {
                Map<String, Object> data = (Map<String, Object>) volResult.get("data");
                List<String> fieldList = (List<String>) data.get("fields");
                List<List<Object>> items = (List<List<Object>>) data.get("items");
                if (!items.isEmpty()) {
                    int idx = fieldList.indexOf("vol");
                    double sum = 0;
                    for (List<Object> item : items) {
                        sum += ((Number) item.get(idx)).doubleValue();
                    }
                    avgVol = sum / items.size();
                }
            }
            // 判断量能是否放大
            boolean result = todayVol > avgVol * 1.2;
            log.debug("股票: {} 量能检查 - 当前量: {}, 5日均量: {}, 放大倍数: {}, 结果: {}", 
                    stock.getCode(), todayVol, avgVol, avgVol == 0 ? 0 : todayVol / avgVol, result);
            return result;
        } catch (Exception e) {
            log.error("调用tushare判断量能放大失败", e);
            return false;
        }
    }

    /**
     * 判断周线MACD是否红柱，基于tushare数据
     */
    @Override
    public boolean isWeekMACDRed(StockInfo stock) {
        log.info("进入 isWeekMACDRed 方法，参数: {}", stock);
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("ts_code", stock.getCode());
            params.put("start_date", LocalDate.now().minusWeeks(2).format(DateTimeFormatter.BASIC_ISO_DATE));
            params.put("end_date", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
            params.put("period", "W");
            Map<String, Object> macdResult = tushareDataService.query("macd", params, "macd,signal,hist");
            // 解析MACD数据
            if (macdResult != null && macdResult.get("code") != null && ((Integer)macdResult.get("code")) == 0) {
                Map<String, Object> data = (Map<String, Object>) macdResult.get("data");
                List<String> fieldList = (List<String>) data.get("fields");
                List<List<Object>> items = (List<List<Object>>) data.get("items");
                if (!items.isEmpty()) {
                    List<Object> last = items.get(items.size() - 1);
                    int macdIdx = fieldList.indexOf("macd");
                    int signalIdx = fieldList.indexOf("signal");
                    int histIdx = fieldList.indexOf("hist");
                    double macd = ((Number) last.get(macdIdx)).doubleValue();
                    double signal = ((Number) last.get(signalIdx)).doubleValue();
                    double hist = ((Number) last.get(histIdx)).doubleValue();
                    // 判断MACD红柱
                    boolean result = macd > signal && hist > 0;
                    log.debug("股票: {} 周线MACD检查 - MACD: {}, Signal: {}, Histogram: {}, 结果: {}", 
                            stock.getCode(), macd, signal, hist, result);
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("调用tushare判断周线MACD红柱失败", e);
        }
        return false;
    }

    /**
     * 获取当前收盘价，基于tushare数据
     */
    @Override
    public double getCurrentPrice(String code) {
        log.info("进入 getCurrentPrice 方法，参数: code={}", code);
        try {
            String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            Map<String, Object> params = new HashMap<>();
            params.put("ts_code", code);
            params.put("start_date", today);
            params.put("end_date", today);
            Map<String, Object> dailyResult = tushareDataService.query("daily", params, "close");
            // 解析收盘价
            if (dailyResult != null && dailyResult.get("code") != null && ((Integer)dailyResult.get("code")) == 0) {
                Map<String, Object> data = (Map<String, Object>) dailyResult.get("data");
                List<String> fieldList = (List<String>) data.get("fields");
                List<List<Object>> items = (List<List<Object>>) data.get("items");
                if (!items.isEmpty()) {
                    int idx = fieldList.indexOf("close");
                    return ((Number) items.get(0).get(idx)).doubleValue();
                }
            }
        } catch (Exception e) {
            log.error("调用tushare获取当前价格失败", e);
        }
        return 0;
    }

    /**
     * 判断15分钟MACD顶背离，基于tushare数据
     */
    @Override
    public boolean isMACDTopDivergence15Min(String code) {
        log.info("进入 isMACDTopDivergence15Min 方法，参数: code={}", code);
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("ts_code", code);
            params.put("start_date", LocalDate.now().minusDays(2).format(DateTimeFormatter.BASIC_ISO_DATE));
            params.put("end_date", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
            params.put("period", "15min");
            Map<String, Object> macdResult = tushareDataService.query("macd", params, "macd,close");
            // 解析15分钟MACD和收盘价
            if (macdResult != null && macdResult.get("code") != null && ((Integer)macdResult.get("code")) == 0) {
                Map<String, Object> data = (Map<String, Object>) macdResult.get("data");
                List<String> fieldList = (List<String>) data.get("fields");
                List<List<Object>> items = (List<List<Object>>) data.get("items");
                if (items.size() >= 2) {
                    // 简单判断：MACD下降但价格上升
                    int macdIdx = fieldList.indexOf("macd");
                    int closeIdx = fieldList.indexOf("close");
                    double macd1 = ((Number) items.get(items.size() - 2).get(macdIdx)).doubleValue();
                    double macd2 = ((Number) items.get(items.size() - 1).get(macdIdx)).doubleValue();
                    double close1 = ((Number) items.get(items.size() - 2).get(closeIdx)).doubleValue();
                    double close2 = ((Number) items.get(items.size() - 1).get(closeIdx)).doubleValue();
                    boolean result = macd2 < macd1 && close2 > close1;
                    log.debug("股票: {} 15分钟MACD顶背离检查 - macd1: {}, macd2: {}, close1: {}, close2: {}, 结果: {}", code, macd1, macd2, close1, close2, result);
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("调用tushare判断15分钟MACD顶背离失败", e);
        }
        return false;
    }
} 