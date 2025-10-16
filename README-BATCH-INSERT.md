# MyBatis 批量插入与拦截器参数计数

这个 demo 演示了如何使用 MyBatis 进行批量插入操作，并在拦截器中捕获 SQL 语句和参数个数。

## 文件说明

1. [BatchUser.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/BatchUser.java) - 批量插入的实体类
2. [BatchUserMapper.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/BatchUserMapper.java) - 批量插入的 Mapper 接口，包含批量插入的 SQL 语句
3. [FirstInterceptor.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/FirstInterceptor.java) - 第一个拦截器，拦截 SQL 并打印参数个数
4. [SecondInterceptor.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/SecondInterceptor.java) - 第二个拦截器，拦截 SQL 并打印参数个数
5. [BatchInsertDemo.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/BatchInsertDemo.java) - 批量插入测试主类
6. [mybatis-config.xml](file:///D:/javaAI/zhipu/src/main/resources/mybatis-config.xml) - MyBatis 配置文件

## 功能说明

### 批量插入 SQL 语句

使用 MyBatis 的 `<script>` 和 `<foreach>` 标签实现批量插入：

```xml
@Insert("<script>" +
        "INSERT INTO batch_user (name, age, email) VALUES " +
        "<foreach collection='users' item='user' separator=','>" +
        "(#{user.name}, #{user.age}, #{user.email})" +
        "</foreach>" +
        "</script>")
int batchInsert(@Param("users") List<BatchUser> users);
```

### 拦截器功能

拦截器会捕获执行的 SQL 语句，并统计参数个数。在批量插入场景中，参数个数即为待插入的记录数。

## 如何运行

1. 确保数据库连接配置正确（在 [mybatis-config.xml](file:///D:/javaAI/zhipu/src/main/resources/mybatis-config.xml) 中配置）
2. 确保数据库中有 `batch_user` 表，表结构包含 `id`, `name`, `age`, `email` 字段
3. 运行 [BatchInsertDemo.java](file:///D:/javaAI/zhipu/src/main/java/com/zhipu/demo/test/BatchInsertDemo.java) 的 main 方法
4. 查看日志输出，观察拦截器捕获的 SQL 语句和参数个数

## 预期输出

运行程序后，您将看到类似以下的日志输出：

```
第一个拦截器：设置属性 - {property1=value1}
第二个拦截器：设置属性 - {property2=value2}
开始测试MyBatis批量插入功能
准备执行批量插入，数据条数: 5
第二个拦截器：开始执行
第二个拦截器：拦截到的SQL: INSERT INTO batch_user (name, age, email) VALUES (?, ?, ?) , (?, ?, ?) , (?, ?, ?) , (?, ?, ?) , (?, ?, ?)
第二个拦截器：参数个数: 5
第一个拦截器：开始执行
第一个拦截器：拦截到的SQL: INSERT INTO batch_user (name, age, email) VALUES (?, ?, ?) , (?, ?, ?) , (?, ?, ?) , (?, ?, ?) , (?, ?, ?)
第一个拦截器：参数个数: 5
第二个拦截器：执行结束，结果: 5
第一个拦截器：执行结束，结果: 5
批量插入完成，影响行数: 5
MyBatis批量插入测试完成
```

## 拦截器执行顺序

根据 MyBatis 的工作机制，拦截器按照在配置文件中声明的顺序执行：

1. FirstInterceptor 开始执行
2. SecondInterceptor 开始执行
3. 实际的数据库操作
4. SecondInterceptor 执行结束
5. FirstInterceptor 执行结束

这是一种典型的"洋葱模型"或"责任链模式"，先执行的拦截器会包裹后执行的拦截器。