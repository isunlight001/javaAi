package com.zhipu.demo.controller;

import com.zhipu.demo.service.DruidPerformanceTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/test/druid")
public class DruidPerformanceTestController {

    @Autowired
    private DruidPerformanceTestService performanceTestService;

    @GetMapping("/performance-comparison")
    public Map<String, Object> testPerformanceComparison() throws ExecutionException, InterruptedException {
        Map<String, Object> result = new HashMap<>();
        
        log.info("开始进行Druid连接池testOnBorrow参数性能对比测试");
        
        // 先测试testOnBorrow=false的情况
        long insertTimeWithoutTest = performanceTestService.testInsertPerformance(false);
        long selectTimeWithoutTest = performanceTestService.testSelectPerformance(false);
        
        // 再测试testOnBorrow=true的情况
        // 注意：这里需要手动修改配置并重启应用才能测试
        // 为模拟测试，我们只测试一种情况，实际使用时需要修改配置后重启
        
        result.put("insert_time_without_test_on_borrow_ms", insertTimeWithoutTest);
        result.put("select_time_without_test_on_borrow_ms", selectTimeWithoutTest);
        result.put("message", "性能测试完成，请手动修改配置测试testOnBorrow=true的情况");
        result.put("note", "需要将application.yml中的test-on-borrow改为true并重启应用后再次测试");
        
        log.info("性能对比测试完成");
        return result;
    }
    
    @GetMapping("/concurrent-insert")
    public Map<String, Object> testConcurrentInsert() throws ExecutionException, InterruptedException {
        Map<String, Object> result = new HashMap<>();
        
        log.info("开始进行并发插入测试");
        
        // 测试不同线程数下的性能
        int[] threadCounts = {1, 2, 4, 8};
        for (int threadCount : threadCounts) {
            long duration = performanceTestService.testConcurrentInsertPerformance(false, threadCount);
            result.put("concurrent_insert_" + threadCount + "_threads_ms", duration);
        }
        
        result.put("message", "并发插入测试完成");
        return result;
    }
}