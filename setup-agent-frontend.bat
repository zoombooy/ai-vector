@echo off
chcp 65001 >nul
echo ========================================
echo Agent前端页面安装脚本
echo ========================================
echo.

cd frontend

echo [1/3] 安装依赖...
call npm install

echo.
echo [2/3] 检查依赖安装...
call npm list @monaco-editor/react

echo.
echo [3/3] 启动开发服务器...
echo.
echo 前端将在 http://localhost:3000 启动
echo 后端API地址: http://localhost:8080
echo.
echo 按任意键启动开发服务器...
pause >nul

call npm run dev

