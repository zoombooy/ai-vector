@echo off
chcp 65001 >nul
echo ========================================
echo 重启 AI-QA-Service
echo ========================================
echo.

cd ai-qa-service

echo [1/2] 编译项目...
call mvn clean compile -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo [2/2] 启动服务...
echo.
echo QA Service 将在 http://localhost:8086 启动
echo.

call mvn spring-boot:run

