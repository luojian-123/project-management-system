# 重启后端服务（Spring Boot）
# 用法：在 D:\PMS系统代码\重启 下执行 .\restart-backend.ps1

$ErrorActionPreference = "SilentlyContinue"
$root = Split-Path $PSScriptRoot -Parent
$backendDir = Join-Path $root "backend"
$jarPath = Join-Path $backendDir "target\pms-backend-1.0.0.jar"

Write-Host "正在停止占用 8080 端口的进程..." -ForegroundColor Yellow
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object {
    Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue
    Write-Host "  已停止进程 $_"
}
Start-Sleep -Seconds 2

if (-not (Test-Path $jarPath)) {
    Write-Host "未找到 jar: $jarPath" -ForegroundColor Red
    Write-Host "请先执行: cd backend; mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

Write-Host "正在启动后端服务 (端口 8080)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendDir'; java -jar target\pms-backend-1.0.0.jar"
Write-Host "后端已在新窗口启动，接口地址: http://localhost:8080/api" -ForegroundColor Green
