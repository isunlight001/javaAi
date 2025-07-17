# 日志配置说明

## 概述

本项目使用Logback作为日志框架，配置了分离的日志输出，将启动日志和应用日志分开存储。

## 启动日志分离的多种实现方式

### 方式一：Spring Boot事件监听器（推荐）
使用 `ApplicationListener<ApplicationReadyEvent>` 在应用启动完成后记录启动信息：

```java
@Slf4j
@Configuration
public class StartupLoggingConfig implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 记录启动完成信息
    }
}
```

### 方式二：CommandLineRunner
使用 `CommandLineRunner` 在应用启动完成后执行启动日志记录：

```java
@Slf4j
@Component
public class StartupRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        // 记录启动完成信息
    }
}
```

### 方式三：自定义Banner
使用自定义Banner在启动时显示应用信息：

```java
public class CustomBanner implements Banner {
    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        // 显示启动信息
    }
}
```

### 方式四：Logback配置分离
通过logback配置将不同包名的日志输出到不同文件。

## 日志文件结构

```
logs/
├── startup.log          # 启动日志（Spring Boot、Spring框架、Hibernate等）
├── application.log      # 应用业务日志
├── error.log           # 错误日志（仅ERROR级别）
├── startup.2024-01-01.0.log  # 启动日志归档文件
├── application.2024-01-01.0.log  # 应用日志归档文件
└── error.2024-01-01.0.log     # 错误日志归档文件
```

## 日志级别配置

### 启动日志（startup.log）
- **Spring Boot**: INFO级别
- **Spring框架**: INFO级别  
- **Hibernate**: INFO级别
- **所有控制台日志**: 包括启动日志和其他非业务日志

### 应用日志（application.log）
- **业务代码（com.zhipu.demo）**: DEBUG级别
- **仅输出到文件，不输出到控制台**

### 错误日志（error.log）
- **所有ERROR级别日志**: 单独存储

## 日志格式

```
时间戳 [线程名] 日志级别 类名 - 日志消息
示例: 2024-01-01 12:00:00.123 [main] INFO  com.zhipu.demo.controller.DemoController - 开始测试日志功能
```

## 日志轮转策略

- **文件大小限制**: 100MB
- **保留天数**: 30天
- **轮转策略**: 按日期和大小轮转

## 测试日志功能

启动应用后，访问以下接口测试日志功能：

```bash
GET http://localhost:8080/api/demo/test-log
```

## 配置说明

### logback-spring.xml 主要配置

1. **日志文件路径**: 配置在项目根目录的 `logs/` 文件夹下
2. **日志格式**: 包含时间戳、线程名、日志级别、类名和消息
3. **日志分离**: 
   - 启动相关日志和其他控制台日志 → startup.log
   - 业务日志 → application.log（仅文件输出）
   - 错误日志 → error.log
4. **控制台输出**: 启动日志和其他非业务日志输出到控制台，业务日志不输出到控制台

### 在代码中使用日志

```java
@Slf4j  // 使用Lombok注解
public class YourService {
    
    public void someMethod() {
        log.trace("TRACE级别日志");
        log.debug("DEBUG级别日志");
        log.info("INFO级别日志");
        log.warn("WARN级别日志");
        log.error("ERROR级别日志");
        
        // 带参数的日志
        log.info("用户ID: {}, 操作: {}", userId, operation);
        
        // 异常日志
        try {
            // 业务逻辑
        } catch (Exception e) {
            log.error("操作失败", e);
        }
    }
}
```

## 注意事项

1. 确保项目根目录有 `logs/` 文件夹的写入权限
2. 日志文件会自动轮转，无需手动清理
3. 生产环境建议调整日志级别为INFO或WARN
4. 错误日志会同时记录到error.log和application.log中 