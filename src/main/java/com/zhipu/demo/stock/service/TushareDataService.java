package com.zhipu.demo.stock.service;

import java.util.Map;

public interface TushareDataService {
    /**
     * 通用Tushare数据查询接口
     * @param apiName tushare接口名
     * @param params  参数map
     * @param fields  字段列表
     * @return 返回原始数据map
     */
    Map<String, Object> query(String apiName, Map<String, Object> params, String fields);
} 