@echo off
echo 智普AI + MySQL 集成 Demo API 测试
echo ====================================

echo.
echo 1. 测试系统信息...
curl -X GET http://localhost:8080/api/demo/info

echo.
echo 2. 测试数据库连接...
curl -X GET http://localhost:8080/api/demo/db-test

echo.
echo 3. 测试AI健康检查...
curl -X GET http://localhost:8080/api/ai/health

echo.
echo 4. 初始化测试用户数据...
curl -X POST http://localhost:8080/api/users/init-test-data

echo.
echo 5. 获取所有用户...
curl -X GET http://localhost:8080/api/users

echo.
echo 6. 测试AI聊天...
curl -X POST http://localhost:8080/api/ai/chat -H "Content-Type: application/json" -d "{\"message\":\"你好，请简单介绍一下自己\"}"

echo.
echo 测试完成！
pause 