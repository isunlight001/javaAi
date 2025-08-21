package com.zhipu.demo.test;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.Random;

@Slf4j
public class DruidFullPerformanceTestScript {

    private static final int INSERT_COUNT = 10000;
    private static final int SELECT_COUNT = 10000;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sc6kd_indv_stl_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String USERNAME = "root";
    private static final String PASSWORD = getDbPassword();

    public static void main(String[] args) throws SQLException {
        log.info("开始Druid连接池testOnBorrow参数完整性能测试");
        log.info("使用数据库URL: {}", DB_URL);
        log.info("使用用户名: {}", USERNAME);
        
        // 检查密码是否设置
        if (PASSWORD == null || PASSWORD.isEmpty()) {
            log.error("数据库密码未设置，请设置DB_PASSWORD环境变量后再运行测试");
            return;
        }

        try {
            // 测试testOnBorrow=false的情况
            PerformanceResult resultWithoutTestOnBorrow = testWithConfig(false);
            log.info("testOnBorrow=false 时:");
            log.info("  插入{}条记录耗时: {} ms", INSERT_COUNT, resultWithoutTestOnBorrow.insertTime);
            log.info("  查询{}次耗时: {} ms", SELECT_COUNT, resultWithoutTestOnBorrow.selectTime);

            // 测试testOnBorrow=true的情况
            PerformanceResult resultWithTestOnBorrow = testWithConfig(true);
            log.info("testOnBorrow=true 时:");
            log.info("  插入{}条记录耗时: {} ms", INSERT_COUNT, resultWithTestOnBorrow.insertTime);
            log.info("  查询{}次耗时: {} ms", SELECT_COUNT, resultWithTestOnBorrow.selectTime);

            // 输出对比结果
            log.info("性能对比结果:");
            log.info("插入操作:");
            log.info("  开启testOnBorrow后性能下降: {} ms", resultWithTestOnBorrow.insertTime - resultWithoutTestOnBorrow.insertTime);
            log.info("  性能下降比例: {}%", 
                    (resultWithTestOnBorrow.insertTime - resultWithoutTestOnBorrow.insertTime) * 100.0 / resultWithoutTestOnBorrow.insertTime);
            
            log.info("查询操作:");
            log.info("  开启testOnBorrow后性能下降: {} ms", resultWithTestOnBorrow.selectTime - resultWithoutTestOnBorrow.selectTime);
            log.info("  性能下降比例: {}", 
                    (resultWithTestOnBorrow.selectTime - resultWithoutTestOnBorrow.selectTime) * 100.0 / resultWithoutTestOnBorrow.selectTime);
        } catch (SQLException e) {
            log.error("测试过程中发生数据库错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    private static PerformanceResult testWithConfig(boolean testOnBorrow) throws SQLException {
        // 创建数据源
        DruidDataSource dataSource = createDataSource(testOnBorrow);

        try {
            // 准备测试环境
            prepareTestEnvironment(dataSource);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            // 测试插入性能
            long insertTime = testInsertPerformance(jdbcTemplate);

            // 测试查询性能
            long selectTime = testSelectPerformance(jdbcTemplate);

            return new PerformanceResult(insertTime, selectTime);
        } finally {
            // 清理资源
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

    private static long testInsertPerformance(JdbcTemplate jdbcTemplate) {
        long startTime = System.currentTimeMillis();

        // 执行插入操作
        for (int i = 0; i < INSERT_COUNT; i++) {
            String name = "User" + i;
            int age = 20 + (i % 50);
            jdbcTemplate.update("INSERT INTO druid_performance_test (name, age) VALUES (?, ?)", name, age);
        }

        return System.currentTimeMillis() - startTime;
    }

    private static long testSelectPerformance(JdbcTemplate jdbcTemplate) {
        Random random = new Random();
        long startTime = System.currentTimeMillis();

        // 执行查询操作
        for (int i = 0; i < SELECT_COUNT; i++) {
            // 随机查询某条记录
            int id = random.nextInt(INSERT_COUNT) + 1;
            jdbcTemplate.query("SELECT * FROM druid_performance_test WHERE id = ?", 
                (rs, rowNum) -> {
                    // 处理结果
                    return rs.getString("name");
                }, 
                id);
        }

        return System.currentTimeMillis() - startTime;
    }

    private static DruidDataSource createDataSource(boolean testOnBorrow) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 连接池配置
        dataSource.setInitialSize(5);
        dataSource.setMinIdle(5);
        dataSource.setMaxActive(20);
        dataSource.setMaxWait(60000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnReturn(false);

        return dataSource;
    }

    private static void prepareTestEnvironment(DruidDataSource dataSource) throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            // 删除测试表（如果存在）
            jdbcTemplate.execute("DROP TABLE IF EXISTS druid_performance_test");
        } catch (Exception e) {
            // 忽略异常
            log.warn("删除测试表时出错，可能是表不存在: {}", e.getMessage());
        }

        // 创建测试表
        jdbcTemplate.execute("CREATE TABLE druid_performance_test (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(50), " +
                "age INT)");
    }

    private static String getDbPassword() {
        // 首先尝试从环境变量获取密码
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            // 如果环境变量未设置，使用默认密码（来自application-secret.yml）
            log.warn("未设置DB_PASSWORD环境变量，将使用默认密码");
            password = "root123456";
        }
        log.info("使用数据库密码: {}", password);
        return password;
    }

    private static class PerformanceResult {
        private final long insertTime;
        private final long selectTime;

        public PerformanceResult(long insertTime, long selectTime) {
            this.insertTime = insertTime;
            this.selectTime = selectTime;
        }
    }
}