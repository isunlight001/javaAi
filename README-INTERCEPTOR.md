# MyBatis 拦截器执行顺序测试

这个 demo 演示了 MyBatis 拦截器的执行顺序。

## 文件说明

1. [FirstInterceptor.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/FirstInterceptor.java) - 第一个拦截器
2. [SecondInterceptor.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/SecondInterceptor.java) - 第二个拦截器
3. [mybatis-config.xml](file:///D:/javaAI/zhipu/src/main/resources/mybatis-config.xml) - MyBatis 配置文件，配置了两个拦截器
4. [InterceptorOrderDemo.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/InterceptorOrderDemo.java) - 测试主类

## 拦截器执行顺序

根据 MyBatis 的工作机制，拦截器按照在配置文件中声明的顺序执行：

1. FirstInterceptor 开始执行
2. SecondInterceptor 开始执行
3. 实际的数据库操作
4. SecondInterceptor 执行结束
5. FirstInterceptor 执行结束

这是一种典型的"洋葱模型"或"责任链模式"，先执行的拦截器会包裹后执行的拦截器。

## 如何运行

1. 确保数据库连接配置正确（在 [mybatis-config.xml](file:///D:/javaAI/zhipu/src/main/resources/mybatis-config.xml) 中配置）
2. 运行 [InterceptorOrderDemo.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/InterceptorOrderDemo.java) 的 main 方法
3. 查看日志输出，观察拦截器的执行顺序

## 验证结果

通过日志输出可以清楚地看到拦截器的执行顺序，验证了 MyBatis 拦截器的工作机制。