package com.zhipu.demo.stockAi;

import com.zhipu.demo.bugfix.ai.AiModelFactory;
import com.zhipu.demo.bugfix.ai.AiModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhipu.demo.stock.service.StockDataService;
import com.zhipu.demo.stock.model.StockInfo;
import com.zhipu.demo.stock.service.TushareDataService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockAiService {
    @Autowired
    private AiModelFactory aiModelFactory;
    @Autowired
    private StockDataService stockDataService;
    @Autowired
    private TushareDataService tushareDataService;

    public Map<String, Object> analyzeStockWithTokenInfo(String stockCode, String modelType) throws Exception {
        String stockInfo = queryStockInfo(stockCode);
        Map<String, Object> result = callLLMWithTokenInfo(stockInfo, modelType);
        return result;
    }

    private String queryStockInfo(String stockCode) {
        // 1. 获取基本信息
        StockInfo info = null;
        StringBuilder sb = new StringBuilder();
        try {
            // 股票基本信息
            java.util.List<StockInfo> all = stockDataService.getAllStocks();
            log.info("[StockAi] 查询到的股票信息:\n{}", all.stream().map(Object::toString).collect(java.util.stream.Collectors.joining("\n")));
            for (StockInfo s : all) {
                if (s.getCode().substring(6).equalsIgnoreCase(stockCode)) {
                    info = s;
                    break;
                }
            }
            if (info != null) {
                sb.append("股票代码: ").append(info.getCode()).append("\n");
                sb.append("公司名称: ").append(info.getName()).append("\n");
                sb.append("行业: ").append(info.getIndustry()).append("\n");
                sb.append("地区: ").append(info.getArea()).append("\n");
                sb.append("上市日期: ").append(info.getListDate()).append("\n");
            } else {
                sb.append("股票代码: ").append(stockCode).append("\n");
            }
            // 2. 获取市盈率/市值等（tushare）
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("ts_code", stockCode);
            java.util.Map<String, Object> result = tushareDataService.query("daily_basic", params, "pe,pe_ttm,total_mv");
            if (result != null && result.get("code") != null && ((Integer)result.get("code")) == 0) {
                java.util.Map<String, Object> data = (java.util.Map<String, Object>) result.get("data");
                java.util.List<String> fieldList = (java.util.List<String>) data.get("fields");
                java.util.List<java.util.List<Object>> items = (java.util.List<java.util.List<Object>>) data.get("items");
                if (!items.isEmpty()) {
                    int peIdx = fieldList.indexOf("pe");
                    int peTtmIdx = fieldList.indexOf("pe_ttm");
                    int mvIdx = fieldList.indexOf("total_mv");
                    sb.append("市盈率: ").append(items.get(0).get(peIdx)).append("\n");
                    sb.append("市盈率TTM: ").append(items.get(0).get(peTtmIdx)).append("\n");
                    sb.append("总市值(万元): ").append(items.get(0).get(mvIdx)).append("\n");
                }
            }
            // 3. 获取最新公告（可扩展）
            // 4. 技术面：当前价、涨跌幅、均线
            double price = stockDataService.getCurrentPrice(stockCode);
            sb.append("当前价: ").append(price).append("\n");
            double change = stockDataService.getTodayChangePercent(info != null ? info : new StockInfo(stockCode, "", false, false));
            sb.append("今日涨跌幅: ").append(change).append("%\n");
            // 5. 其他可扩展指标
        } catch (Exception e) {
            sb.append("[获取股票信息异常]: ").append(e.getMessage()).append("\n");
        }
        sb.append("请基于上述信息，结合大模型能力，给出该股票的投资分析、风险提示和操作建议，结构化输出。");
        String stockInfo = sb.toString();
        log.info("[StockAi] 查询到的股票信息:\n{}", stockInfo);
        return stockInfo;
    }

    private Map<String, Object> callLLMWithTokenInfo(String stockInfo, String modelType) throws Exception {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        AiModelService aiService = modelType != null ?
                aiModelFactory.getAiModelService(modelType) :
                aiModelFactory.getAiModelService();
        log.info("=== Stock AI调用开始 ===");
        log.info("使用模型: {}", aiService.getModelName());
        log.info("时间: {}", timestamp);
        log.info("股票信息长度: {} 字符", stockInfo.length());
        log.info("预估输入token数: {}", estimateTokenCount(stockInfo));
        try {
            String prompt = "# 股票智能分析\n" +
                    "你是一位资深证券分析师，擅长结合基本面、技术面和大模型能力进行投资决策。请对以下股票信息进行全面分析，输出结构化JSON，包含：\n" +
                    "- 股票摘要（公司、行业、主营、财务、技术面等）\n" +
                    "- 投资亮点\n" +
                    "- 风险提示\n" +
                    "- 操作建议（买入/观望/减持，理由）\n" +
                    "- 结构化输出示例：\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"summary\": \"...\",\n" +
                    "  \"highlights\": [\"...\"],\n" +
                    "  \"risks\": [\"...\"],\n" +
                    "  \"advice\": {\n" +
                    "    \"action\": \"buy/hold/sell\",\n" +
                    "    \"reason\": \"...\"\n" +
                    "  }\n" +
                    "}\n" +
                    "```\n" +
                    "请分析以下股票信息：\n" + stockInfo;
            log.info("提示词长度: {} 字符", prompt.length());
            log.info("预估总输入token数: {}", estimateTokenCount(prompt));
            String response = aiService.chat(prompt);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            int inputTokens = estimateTokenCount(prompt);
            int outputTokens = estimateTokenCount(response);
            int totalTokens = inputTokens + outputTokens;
            double speed = (double) response.length() / (duration / 1000.0);
            log.info("=== Stock AI调用完成 ===");
            log.info("响应时间: {} ms", duration);
            log.info("响应内容长度: {} 字符", response.length());
            log.info("预估输出token数: {}", outputTokens);
            log.info("预估总token消耗: {}", totalTokens);
            log.info("平均响应速度: {:.2f} 字符/秒", speed);
            Map<String, Object> result = new HashMap<>();
            result.put("suggestion", response);
            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("inputTokens", inputTokens);
            tokenInfo.put("outputTokens", outputTokens);
            tokenInfo.put("totalTokens", totalTokens);
            tokenInfo.put("responseTime", duration);
            tokenInfo.put("speed", String.format("%.2f", speed));
            result.put("tokenInfo", tokenInfo);
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.error("=== Stock AI调用失败 ===");
            log.error("响应时间: {} ms", duration);
            log.error("异常信息: {}", e.getMessage(), e);
            throw e;
        }
    }

    private int estimateTokenCount(String text) {
        if (text == null) return 0;
        return (int) Math.ceil(text.length() / 4.0);
    }
} 