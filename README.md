# 智普AI + MySQL 集成 Demo

Spring Boot 项目，集成智普AI和MySQL数据库。

## 功能
- MySQL 数据库连接和操作
- 智普AI API 调用
- **智能查询功能** - 优先查询本地数据库，结合AI提供准确回答
- 用户管理功能
- AI 数据分析

## 快速开始

1. 确保MySQL运行，数据库 `sc6kd_indv_stl_db` 已创建
2. 进入项目目录：`cd zhipu`
3. 启动项目：`mvn spring-boot:run`
4. 访问前端界面：http://localhost:8080

## 前端界面

项目启动后，访问 http://localhost:8080 即可使用Web界面：

- **系统演示**：系统信息、数据库测试、用户数据分析
- **用户管理**：查看用户、创建用户、初始化测试数据
- **AI服务**：智能查询（推荐）、AI聊天、数据分析

## 主要API

- 系统信息：`GET /api/demo/info`
- 数据库测试：`GET /api/demo/db-test`
- 智能查询：`POST /api/ai/smart-query`
- AI聊天：`POST /api/ai/chat`
- 用户管理：`GET /api/users`

## 配置
- MySQL: localhost:3306/sc6kd_indv_stl_db
- 智普AI: 已配置API Key (v4 API)
- 端口: 8080

### AI服务配置
在 `application.yml` 中可以配置：
- `zhipu.ai.use-mock: true` - 使用模拟AI服务（推荐）
- `zhipu.ai.use-mock: false` - 使用真实智普AI API

## 故障排除

### AI服务问题
- 如果智普AI API调用失败，系统会自动使用模拟AI服务
- 检查API Key是否正确
- 确认网络连接正常
- 模拟AI服务提供基本的聊天和数据分析功能

### Bean定义冲突
- 已修复MockAiService重复定义问题
- 系统现在使用内置的模拟服务作为备用方案

### 数据库连接问题
- 确保MySQL服务运行
- 检查数据库连接信息
- 确认数据库已创建

## 技术栈

- **后端框架**: Spring Boot 2.7.0
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **AI服务**: 智普AI (GLM-4)
- **构建工具**: Maven
- **开发语言**: Java 8

## 项目结构

```
zhipu/
├── src/main/java/com/zhipu/demo/
│   ├── ZhipuDemoApplication.java          # 启动类
│   ├── config/
│   │   └── ZhipuAiConfig.java             # 智普AI配置
│   ├── controller/
│   │   ├── UserController.java            # 用户管理API
│   │   ├── AiController.java              # AI服务API
│   │   └── DemoController.java            # 演示功能API
│   ├── dto/
│   │   ├── ZhipuAiRequest.java            # AI请求DTO
│   │   └── ZhipuAiResponse.java           # AI响应DTO
│   ├── entity/
│   │   └── User.java                      # 用户实体
│   ├── repository/
│   │   └── UserRepository.java            # 数据访问层
│   └── service/
│       ├── UserService.java               # 用户服务
│       └── ZhipuAiService.java            # AI服务
├── src/main/resources/
│   └── application.yml                    # 配置文件
├── pom.xml                                # Maven配置
└── README.md                              # 项目说明
```

## 使用示例

### 1. 创建用户

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "测试用户",
    "email": "test@example.com",
    "description": "这是一个测试用户"
  }'
```

### 2. 智能查询

```bash
curl -X POST http://localhost:8080/api/ai/smart-query \
  -H "Content-Type: application/json" \
  -d '{
    "message": "查询所有用户信息"
  }'
```

### 3. AI聊天

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好，请介绍一下自己"
  }'
```

### 4. 快速测试

```bash
curl -X POST http://localhost:8080/api/demo/quick-test \
  -H "Content-Type: application/json" \
  -d '{
    "username": "新用户",
    "email": "newuser@example.com",
    "description": "这是一个新用户，擅长Java开发"
  }'
```

## 注意事项

1. **数据库连接**: 确保MySQL服务正常运行，数据库 `sc6kd_indv_stl_db` 已创建
2. **智普AI API**: 项目已配置API Key，如需更换请修改 `application.yml`
3. **端口占用**: 默认使用8080端口，如需修改请更改配置文件
4. **日志级别**: 开发环境已开启DEBUG日志，生产环境建议调整

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 验证数据库连接信息是否正确
   - 确认数据库是否存在

2. **AI服务调用失败**
   - 检查网络连接
   - 验证API Key是否有效
   - 查看日志获取详细错误信息

3. **端口被占用**
   - 修改 `application.yml` 中的端口配置
   - 或停止占用端口的其他服务

## 开发说明

### 添加新功能

1. 在 `entity` 包下创建实体类
2. 在 `repository` 包下创建数据访问接口
3. 在 `service` 包下创建业务逻辑
4. 在 `controller` 包下创建API接口

### 扩展AI功能

1. 在 `ZhipuAiService` 中添加新的AI方法
2. 在 `AiController` 中添加对应的API接口
3. 根据需要创建新的DTO类

## 许可证

本项目仅供学习和演示使用。 