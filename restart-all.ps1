# 重启前后端服务（先停 8080、5173，再分别在新窗口启动后端和前端）
# 用法：在项目根目录或 D:\PMS系统代码 下，PowerShell 执行 .\restart-all.ps1

$ErrorActionPreference = "SilentlyContinue"
$root = if ($PSScriptRoot) { $PSScriptRoot } else { Get-Location }
$backendDir = Join-Path $root "backend"
$frontendDir = Join-Path $root "frontend"

Write-Host "========== 停止旧进程 ==========" -ForegroundColor Cyan
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
Write-Host "已释放 8080、5173 端口" -ForegroundColor Yellow
Start-Sleep -Seconds 2

Write-Host "========== 启动后端 ==========" -ForegroundColor Cyan
$jarPath = Join-Path $backendDir "target\pms-backend-1.0.0.jar"
if (Test-Path $jarPath) {
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendDir'; java -jar target\pms-backend-1.0.0.jar"
    Write-Host "后端已在新窗口启动 -> http://localhost:8080/api" -ForegroundColor Green
} else {
    Write-Host "未找到 jar，请先在 backend 目录执行: mvn clean package -DskipTests" -ForegroundColor Red
}

Write-Host "========== 启动前端 ==========" -ForegroundColor Cyan
if (Test-Path (Join-Path $frontendDir "package.json")) {
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendDir'; npm run dev"
    Write-Host "前端已在新窗口启动 -> http://localhost:5173/" -ForegroundColor Green
} else {
    Write-Host "未找到 frontend/package.json" -ForegroundColor Red
}

Write-Host "`n完成。请在新打开的窗口中查看运行日志。" -ForegroundColor Cyan
