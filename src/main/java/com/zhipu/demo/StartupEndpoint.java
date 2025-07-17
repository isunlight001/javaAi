package com.zhipu.demo;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@RestControllerEndpoint(id = "startup")
@Component
public class StartupEndpoint {
    private final Map<String, Long> beanLoadTimes;
    @Autowired
    public StartupEndpoint(Map<String, Long> beanLoadTimes) {
        this.beanLoadTimes = beanLoadTimes;
    }
    @GetMapping
    @ResponseBody
    public String beans() {
        List<Map<String, Object>> beans = new ArrayList<>();
        long totalTime = 0;
        for (Map.Entry<String, Long> entry : beanLoadTimes.entrySet()) {
            Map<String, Object> bean = new HashMap<>();
            bean.put("bean", entry.getKey());
            bean.put("loadTimeMs", entry.getValue());
            beans.add(bean);
            totalTime += entry.getValue();
        }
        beans.sort(Comparator.comparingLong(b -> (Long) b.get("loadTimeMs")));
        int beanCount = beans.size();
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Bean加载耗时</title><style>");
        html.append("body{font-family:Arial,sans-serif;background:#f7f8fa;}table{border-collapse:collapse;margin:30px auto;background:#fff;border-radius:10px;box-shadow:0 2px 12px rgba(0,0,0,0.08);}th,td{padding:10px 18px;}th{background:#667eea;color:#fff;}tr:nth-child(even){background:#f3f6fa;}tr:hover{background:#e0e7ff;}td{border-bottom:1px solid #eee;}</style></head><body>");
        html.append("<h2 style='text-align:center;color:#667eea;'>Spring Bean 加载耗时统计</h2>");
        html.append("<div style='text-align:center;margin-bottom:18px;color:#444;font-size:16px;'>");
        html.append("Bean总数量: <b>").append(beanCount).append("</b> &nbsp; | &nbsp; 加载总耗时: <b>").append(totalTime).append(" ms</b></div>");
        html.append("<table><tr><th>Bean 名称</th><th>加载耗时 (ms)</th></tr>");
        for (Map<String, Object> bean : beans) {
            html.append("<tr><td>").append(bean.get("bean")).append("</td><td>").append(bean.get("loadTimeMs")).append("</td></tr>");
        }
        html.append("</table></body></html>");
        return html.toString();
    }
} 