package com.zhipu.demo.controller;

import com.zhipu.demo.entity.NiusanStock;
import com.zhipu.demo.service.NiusanStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/niusan")
public class NiusanStockController {
    @Autowired
    private NiusanStockService service;

    // 上传并解析表格图片后的文本（假设前端已OCR为文本）
    @PostMapping("/upload")
    public String upload(@RequestBody List<List<String>> table) {
        List<NiusanStock> list = new ArrayList<>();
        for (List<String> row : table) {
            if (row.size() < 8 || row.get(0).contains("股票")) continue;
            // 股票名称仅保留中文字符
            String name = row.get(0).replaceAll("[0-9a-zA-Z.。·,，、\\s\\-]+", "").trim();
            String code = "";
            // 尝试从第1列提取6位数字代码，否则取第2列
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{6})").matcher(row.get(0));
            if (m.find()) code = m.group(1);
            else if (row.size() > 1) code = row.get(1).replaceAll("\\D", "");
            NiusanStock s = new NiusanStock();
            s.setName(name);
            s.setCode(code);
            s.setHoldNum(parseBig(row.get(2)));
            s.setChangeRate(row.get(3));
            s.setPercent(row.get(4));
            s.setChangeType(row.get(5));
            s.setMarketCap(parseBig(row.get(6)));
            s.setReportDate(row.get(7));
            list.add(s);
        }
        service.saveAll(list);
        return "ok";
    }

    // 查询所有牛散股票数据
    @GetMapping("/list")
    public List<NiusanStock> list() {
        return service.findAll();
    }

    private BigDecimal parseBig(String s) {
        try { return new BigDecimal(s.replaceAll("[^\\d.]+", "")); } catch (Exception e) { return BigDecimal.ZERO; }
    }
} 