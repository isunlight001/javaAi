package com.zhipu.demo.test;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

@Slf4j
public class DruidPerformanceTestScript {

    private static final int RECORD_COUNT = 10000;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sc6kd_indv_stl_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String USERNAME = "root";
    private static final String PASSWORD = getDbPassword();

    public static void main(String[] args) throws SQLException {
        log.info("开始Druid连接池testOnBorrow参数性能测试");
        log.info("使用数据库URL: {}", DB_URL);
        log.info("使用用户名: {}", USERNAME);
        
        // 检查密码是否设置
        if (PASSWORD == null || PASSWORD.isEmpty()) {
            log.error("数据库密码未设置，请设置DB_PASSWORD环境变量后再运行测试");
            return;
        }

        try {
            // 测试testOnBorrow=false的情况
            long timeWithoutTestOnBorrow = testWithConfig(false);
            log.info("testOnBorrow=false 时，插入{}条记录耗时: {} ms", RECORD_COUNT, timeWithoutTestOnBorrow);

            // 测试testOnBorrow=true的情况
            long timeWithTestOnBorrow = testWithConfig(true);
            log.info("testOnBorrow=true 时，插入{}条记录耗时: {} ms", RECORD_COUNT, timeWithTestOnBorrow);

            // 输出对比结果
            log.info("性能对比结果:");
            log.info("开启testOnBorrow后性能下降: {} ms", timeWithTestOnBorrow - timeWithoutTestOnBorrow);
            log.info("性能下降比例: {}%", 
                    (timeWithTestOnBorrow - timeWithoutTestOnBorrow) * 100.0 / timeWithoutTestOnBorrow);
        } catch (SQLException e) {
            log.error("测试过程中发生数据库错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    private static long testWithConfig(boolean testOnBorrow) throws SQLException {
        // 创建数据源
        DruidDataSource dataSource = createDataSource(testOnBorrow);

        try {
            // 准备测试环境
            prepareTestEnvironment(dataSource);

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            long startTime = System.currentTimeMillis();

            // 执行插入操作
            for (int i = 0; i < RECORD_COUNT; i++) {
                String name = "User" + i;
                int age = 20 + (i % 50);
                jdbcTemplate.update("INSERT INTO druid_performance_test (name, age) VALUES (?, ?)", name, age);
            }

            long endTime = System.currentTimeMillis();

            return endTime - startTime;
        } finally {
            // 清理资源
            if (dataSource != null) {
                dataSource.close();
            }
        }
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
//        dataSource.setValidationQuery("SELECT 1");
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
}