# Spring @Value 注解获取 String 数组 Demo

这个 demo 演示了如何使用 Spring 的 @Value 注解获取 String 数组，并实现静态加载访问。

## 文件说明

1. [ValueArrayConfig.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/ValueArrayConfig.java) - 配置类，使用 @Value 注解获取数组并实现静态加载
2. [ValueArrayDemo.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/ValueArrayDemo.java) - 主程序类，演示如何访问 @Value 加载的数组
3. [application.properties](file:///D:/javaAI/zhipu/src/main/resources/application.properties) - 配置文件，定义数组值
4. [pom.xml](file:///D:/javaAI/zhipu/pom.xml) - Maven 配置文件

## 功能说明

### @Value 注解获取数组

在 Spring 中，可以通过 @Value 注解直接将配置文件中的逗号分隔字符串转换为数组或列表：

```java
// 获取String数组
@Value("${app.test-array:default1,default2,default3}")
private String[] testArray;

// 获取List
@Value("${app.test-list:defaultA,defaultB,defaultC}")
private List<String> testList;
```

### 静态加载实现

为了在静态上下文中访问 @Value 注解加载的值，我们在 Bean 初始化时将值保存到静态变量中：

```java
@PostConstruct
public void init() {
    // 在Bean初始化时将值赋给静态变量
    staticTestArray = this.testArray;
    staticTestList = this.testList;
}
```

## 配置文件

在 [application.properties](file:///D:/javaAI/zhipu/src/main/resources/application.properties) 中定义数组值：

```properties
# 测试数组配置
app.test-array=item1,item2,item3,item4,item5
app.test-list=valueA,valueB,valueC,valueD
```

## 如何运行

1. 确保项目依赖完整（Spring Boot 相关依赖）
2. 运行 [ValueArrayDemo.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/ValueArrayDemo.java) 的 main 方法
3. 查看控制台输出，观察 @Value 注解加载的数组值

## 预期输出

运行程序后，您将看到类似以下的日志输出：

```
2023-xx-xx xx:xx:xx.xxx [main] INFO com.zhipu.demo.test.ValueArrayConfig - 从配置文件加载的数组值: [item1, item2, item3, item4, item5]
2023-xx-xx xx:xx:xx.xxx [main] INFO com.zhipu.demo.test.ValueArrayConfig - 从配置文件加载的列表值: [valueA, valueB, valueC, valueD]
2023-xx-xx xx:xx:xx.xxx [main] INFO com.zhipu.demo.test.ValueArrayDemo - === 静态访问 @Value 数组值 ===
2023-xx-xx xx:xx:xx.xxx [main] INFO com.zhipu.demo.test.ValueArrayDemo - 静态访问数组值: [item1, item2, item3, item4, item5]
2023-xx-xx xx:xx:xx.xxx [main] INFO com.zhipu.demo.test.ValueArrayDemo - 静态访问列表值: [valueA, valueB, valueC, valueD]
2023-xx-xx xx:xx:xx.xxx [main] INFO com.zhipu.demo.test.ValueArrayDemo - === 通过Spring容器访问 @Value 数组值 ===
2023-xx-xx xx:xx:xx.xxx [main] INFO com.zhipu.demo.test.ValueArrayDemo - 通过Bean访问数组值: [item1, item2, item3, item4, item5]
2023-xx-xx xx:xx:xx.xxx [main] INFO com.zhipu.demo.test.ValueArrayDemo - 通过Bean访问列表值: [valueA, valueB, valueC, valueD]
```

## 注意事项

1. @Value 注解只能在 Spring 管理的 Bean 中使用
2. 要在静态上下文中访问 @Value 加载的值，需要在 Bean 初始化完成后将其保存到静态变量中
3. 配置文件中的数组值应使用逗号分隔
4. 可以为 @Value 注解提供默认值，格式为 `${key:defaultValue}`