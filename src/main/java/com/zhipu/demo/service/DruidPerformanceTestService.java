package com.zhipu.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class DruidPerformanceTestService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;
    
    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * 测试插入1万条数据的性能
     *
     * @param testOnBorrow 是否开启testOnBorrow
     * @return 耗时（毫秒）
     */
    public long testInsertPerformance(boolean testOnBorrow) {
        // 准备测试环境
        prepareTestEnvironment();

        log.info("开始测试插入1万条数据性能，testOnBorrow={}", testOnBorrow);

        long startTime = System.currentTimeMillis();

        // 执行插入操作
        for (int i = 0; i < 10000; i++) {
            String name = "User" + i;
            int age = 20 + (i % 50);
            jdbcTemplate.update("INSERT INTO performance_test (name, age) VALUES (?, ?)", name, age);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("插入1万条数据完成，testOnBorrow={}，耗时: {} ms", testOnBorrow, duration);

        return duration;
    }

    /**
     * 测试查询1万条数据的性能
     *
     * @param testOnBorrow 是否开启testOnBorrow
     * @return 耗时（毫秒）
     */
    public long testSelectPerformance(boolean testOnBorrow) {
        log.info("开始测试查询1万条数据性能，testOnBorrow={}", testOnBorrow);

        long startTime = System.currentTimeMillis();

        // 执行查询操作
        for (int i = 0; i < 10000; i++) {
            jdbcTemplate.query("SELECT * FROM performance_test WHERE id = ?", 
                (rs, rowNum) -> {
                    // 处理结果集
                    return rs.getString("name");
                }, 
                (i % 1000) + 1); // 查询前1000条记录中的某一条，增加随机性
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("查询1万条数据完成，testOnBorrow={}，耗时: {} ms", testOnBorrow, duration);

        return duration;
    }

    /**
     * 并发测试插入性能
     *
     * @param testOnBorrow 是否开启testOnBorrow
     * @param threadCount  并发线程数
     * @return 耗时（毫秒）
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public long testConcurrentInsertPerformance(boolean testOnBorrow, int threadCount) 
            throws InterruptedException, ExecutionException {
        
        prepareTestEnvironment();
        
        log.info("开始并发测试插入数据性能，testOnBorrow={}，线程数={}", testOnBorrow, threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Long>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // 提交多个任务并发执行
        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            Future<Long> future = executor.submit(() -> {
                long threadStartTime = System.currentTimeMillis();
                for (int i = 0; i < 10000 / threadCount; i++) {
                    String name = "User_Thread" + threadIndex + "_" + i;
                    int age = 20 + (i % 50);
                    jdbcTemplate.update("INSERT INTO performance_test (name, age) VALUES (?, ?)", name, age);
                }
                return System.currentTimeMillis() - threadStartTime;
            });
            futures.add(future);
        }

        // 等待所有任务完成
        for (Future<Long> future : futures) {
            future.get();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();
        
        log.info("并发插入测试完成，testOnBorrow={}，线程数={}，耗时: {} ms", testOnBorrow, threadCount, duration);

        return duration;
    }

    /**
     * 准备测试环境
     */
    private void prepareTestEnvironment() {
        try {
            // 删除测试表（如果存在）
            jdbcTemplate.execute("DROP TABLE IF EXISTS performance_test");
            
            // 创建测试表
            jdbcTemplate.execute("CREATE TABLE performance_test (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50), " +
                    "age INT)");
            
            // 预先插入一些数据用于查询测试
            for (int i = 0; i < 1000; i++) {
                String name = "PreUser" + i;
                int age = 20 + (i % 50);
                jdbcTemplate.update("INSERT INTO performance_test (name, age) VALUES (?, ?)", name, age);
            }
        } catch (Exception e) {
            log.error("准备测试环境时出错", e);
        }
    }
}