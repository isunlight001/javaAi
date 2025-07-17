@echo off
echo 测试修复后的功能
echo ===================

echo.
echo 1. 测试系统信息...
curl -X GET http://localhost:8080/api/demo/info

echo.
echo 2. 测试数据库连接...
curl -X GET http://localhost:8080/api/demo/db-test

echo.
echo 3. 测试AI聊天（使用模拟服务）...
curl -X POST http://localhost:8080/api/ai/chat -H "Content-Type: application/json" -d "{\"message\":\"你好\"}"

echo.
echo 4. 测试用户数据分析...
curl -X POST http://localhost:8080/api/ai/analyze-users -H "Content-Type: application/json" -d "张三-开发工程师"

echo.
echo 测试完成！如果看到模拟AI的回复，说明修复成功。
pause 