package com.zhipu.demo.entity;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "niusan_stock")
public class NiusanStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;         // 股票名称
    private String code;         // 股票代码
    private BigDecimal holdNum;  // 持股数量（万股）
    private String changeRate;   // 至今涨幅
    private String percent;      // 占总股本比例
    private String changeType;   // 持股变动
    private BigDecimal marketCap;// 总市值
    private String reportDate;   // 报告期

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getHoldNum() { return holdNum; }
    public void setHoldNum(BigDecimal holdNum) { this.holdNum = holdNum; }
    public String getChangeRate() { return changeRate; }
    public void setChangeRate(String changeRate) { this.changeRate = changeRate; }
    public String getPercent() { return percent; }
    public void setPercent(String percent) { this.percent = percent; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public BigDecimal getMarketCap() { return marketCap; }
    public void setMarketCap(BigDecimal marketCap) { this.marketCap = marketCap; }
    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }
} 