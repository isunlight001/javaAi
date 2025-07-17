@echo off
echo 智普AI + MySQL 集成 Demo 快速启动
echo ===================================

echo.
echo 正在编译项目...
call mvn clean compile

if %ERRORLEVEL% NEQ 0 (
    echo 编译失败，请检查错误信息
    pause
    exit /b 1
)

echo.
echo 编译成功！正在启动项目...
echo.
echo 项目启动后，请访问：http://localhost:8080
echo.

call mvn spring-boot:run

echo.
echo 项目已停止运行
pause 