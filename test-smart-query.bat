@echo off
echo 测试智能查询功能...
echo.

echo 1. 测试查询所有用户信息
curl -X POST http://localhost:8080/api/ai/smart-query -H "Content-Type: application/json" -d "{\"message\": \"查询所有用户信息\"}"
echo.
echo.

echo 2. 测试查询Java开发工程师
curl -X POST http://localhost:8080/api/ai/smart-query -H "Content-Type: application/json" -d "{\"message\": \"查询Java开发工程师有哪些\"}"
echo.
echo.

echo 3. 测试查询前端工程师
curl -X POST http://localhost:8080/api/ai/smart-query -H "Content-Type: application/json" -d "{\"message\": \"查询前端工程师信息\"}"
echo.
echo.

echo 4. 测试查询数据库管理员
curl -X POST http://localhost:8080/api/ai/smart-query -H "Content-Type: application/json" -d "{\"message\": \"查询数据库管理员\"}"
echo.
echo.

echo 5. 测试查询特定用户（假设有张三这个用户）
curl -X POST http://localhost:8080/api/ai/smart-query -H "Content-Type: application/json" -d "{\"message\": \"查询张三的信息\"}"
echo.
echo.

echo 智能查询测试完成！
pause 