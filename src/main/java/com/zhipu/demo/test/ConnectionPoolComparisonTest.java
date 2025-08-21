package com.zhipu.demo.test;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
public class ConnectionPoolComparisonTest {

    private static final int RECORD_COUNT = 100000;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/sc6kd_indv_stl_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String USERNAME = "root";
    private static final String PASSWORD = getDbPassword();

    public static void main(String[] args) throws SQLException {
        log.info("开始Druid和HikariCP连接池性能对比测试");
        log.info("使用数据库URL: {}", DB_URL);
        log.info("使用用户名: {}", USERNAME);

        // 检查密码是否设置
        if (PASSWORD == null || PASSWORD.isEmpty()) {
            log.error("数据库密码未设置，请设置DB_PASSWORD环境变量后再运行测试");
            return;
        }

        try {
            // 测试HikariCP连接池
            long hikariCPTime = testWithHikariCP();
            // 测试Druid连接池
            long druidTime = testWithDruid();
            log.info("使用HikariCP连接池插入{}条记录耗时: {} ms", RECORD_COUNT, hikariCPTime);
            log.info("使用Druid连接池插入{}条记录耗时: {} ms", RECORD_COUNT, druidTime);

            // 输出对比结果
            log.info("性能对比结果:");
            log.info("Druid比HikariCP慢: {} ms", druidTime - hikariCPTime);
            log.info("Druid性能下降比例: {}%", (druidTime - hikariCPTime) * 100.0 / hikariCPTime);
            
        } catch (SQLException e) {
            log.error("测试过程中发生数据库错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    private static long testWithHikariCP() throws SQLException {
        HikariDataSource dataSource = createHikariDataSource();
        
        try {
            prepareTestEnvironment(dataSource);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            long startTime = System.currentTimeMillis();

            // 执行插入操作
            for (int i = 0; i < RECORD_COUNT; i++) {
                String name = "User_Hikari_" + i;
                int age = 20 + (i % 50);
                jdbcTemplate.update("INSERT INTO connection_pool_test (name, age) VALUES (?, ?)", name, age);
            }

            long endTime = System.currentTimeMillis();
            return endTime - startTime;
        } finally {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        }
    }

    private static long testWithDruid() throws SQLException {
        DruidDataSource dataSource = createDruidDataSource();
        
        try {
            prepareTestEnvironment(dataSource);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            long startTime = System.currentTimeMillis();

            // 执行插入操作
            for (int i = 0; i < RECORD_COUNT; i++) {
                String name = "User_Druid_" + i;
                int age = 20 + (i % 50);
                jdbcTemplate.update("INSERT INTO connection_pool_test (name, age) VALUES (?, ?)", name, age);
            }

            long endTime = System.currentTimeMillis();
            return endTime - startTime;
        } finally {
            if (dataSource != null) {
                dataSource.close();
            }
        }
    }

    private static HikariDataSource createHikariDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(DB_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // HikariCP连接池配置
        dataSource.setMaximumPoolSize(20);
        dataSource.setMinimumIdle(5);
        dataSource.setConnectionTimeout(60000);
        dataSource.setIdleTimeout(300000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setLeakDetectionThreshold(60000);

        return dataSource;
    }

    private static DruidDataSource createDruidDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Druid连接池配置
        dataSource.setInitialSize(5);
        dataSource.setMinIdle(5);
        dataSource.setMaxActive(20);
        dataSource.setMaxWait(60000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
//        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(false);

        return dataSource;
    }

    private static void prepareTestEnvironment(DataSource dataSource) throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            // 删除测试表（如果存在）
            jdbcTemplate.execute("DROP TABLE IF EXISTS connection_pool_test");
        } catch (Exception e) {
            // 忽略异常
            log.warn("删除测试表时出错，可能是表不存在: {}", e.getMessage());
        }

        // 创建测试表
        jdbcTemplate.execute("CREATE TABLE connection_pool_test (" +
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