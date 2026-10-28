@echo off
chcp 65001 >nul
title Blog Demo Launcher
color 0A

set "BACKEND=D:\javacode\shixun\blog-system"
set "FRONTEND=D:\javacode\shixun\blog-frontend"
set "JAVA=D:\java\jdk-21\bin\java.exe"
set "NODE=D:\AI\nodejs\node.exe"

echo ============================================================
echo            博客系统  -  答辩演示环境一键启动
echo ============================================================
echo.

REM ---------- 1. 检查 MySQL ----------
echo [1/4] 检查 MySQL 服务...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo       MySQL80 未运行，正在启动...
    net start MySQL80
    if errorlevel 1 (
        echo.
        echo   [X] MySQL 启动失败，请右键本文件选"以管理员身份运行"
        echo.
        pause
        exit /b 1
    )
)
echo       MySQL80 运行中
echo.

REM ---------- 2. 输入数据库密码 ----------
echo [2/4] 启动后端需要数据库密码（仅本次运行使用，不会保存到任何文件）
set /p DB_PASSWORD=      请输入 MySQL root 密码:
if "%DB_PASSWORD%"=="" (
    echo.
    echo   [X] 密码不能为空
    pause
    exit /b 1
)
echo.

REM ---------- 3. 检查 jar ----------
echo [3/4] 检查后端 jar...
if not exist "%BACKEND%\target\blog-system-1.0.0.jar" (
    echo       jar 不存在，正在打包（请稍候，约 30 秒）...
    pushd "%BACKEND%"
    call "D:\java\apache-maven-3.8.8\bin\mvn.cmd" -B -DskipTests package
    popd
)
if not exist "%BACKEND%\target\blog-system-1.0.0.jar" (
    echo   [X] 打包失败，请检查 Maven 环境
    pause
    exit /b 1
)
echo       jar 就绪
echo.

REM ---------- 4. 启动前后端 ----------
echo [4/4] 启动服务...
start "后端-9090" cmd /k "title 后端 9090 && set DB_PASSWORD=%DB_PASSWORD% && cd /d %BACKEND% && %JAVA% -jar target\blog-system-1.0.0.jar --server.port=9090"

timeout /t 6 /nobreak >nul

start "前端-5173" cmd /k "title 前端 5173 && cd /d %FRONTEND% && %NODE% node_modules\vite\bin\vite.js --port 5173"

echo.
echo ============================================================
echo   正在启动，请等待两个窗口都出现 "Started" 字样
echo.
echo   后端地址：http://localhost:9090
echo   前端地址：http://localhost:5173     ^<-- 浏览器打开这个
echo.
echo   演示账号：admin / admin123         （管理员）
echo             zhangsan / abc123456     （普通用户）
echo ============================================================
echo.

timeout /t 8 /nobreak >nul
start "" http://localhost:5173

echo 浏览器已打开。关掉两个黑窗口即可停止服务。
echo.
pause
