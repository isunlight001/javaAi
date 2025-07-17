@echo off
echo 智普AI + MySQL 集成 Demo 启动脚本
echo ====================================

echo.
echo 正在启动项目...
echo 请确保MySQL服务已启动，数据库 sc6kd_indv_stl_db 已创建
echo.

mvn spring-boot:run

echo.
echo 项目启动完成！
echo 前端界面地址: http://localhost:8080
echo 按任意键退出...
pause 