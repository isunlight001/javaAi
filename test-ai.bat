@echo off
echo AI服务测试脚本
echo ================

echo.
echo 1. 测试AI健康检查...
curl -X GET http://localhost:8080/api/ai/health

echo.
echo 2. 测试AI聊天（模拟服务）...
curl -X POST http://localhost:8080/api/ai/chat -H "Content-Type: application/json" -d "{\"message\":\"你好，请介绍一下自己\"}"

echo.
echo 3. 测试用户数据分析...
curl -X POST http://localhost:8080/api/ai/analyze-users -H "Content-Type: application/json" -d "张三-开发工程师，李四-前端工程师"

echo.
echo 测试完成！
echo 如果看到模拟AI的回复，说明配置正确。
pause 