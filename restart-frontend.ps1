# 重启前端开发服务（Vite）
# 用法：在项目根目录或 D:\PMS系统代码 下，PowerShell 执行 .\restart-frontend.ps1

$ErrorActionPreference = "SilentlyContinue"
$root = if ($PSScriptRoot) { $PSScriptRoot } else { Get-Location }
$frontendDir = Join-Path $root "frontend"

Write-Host "正在停止占用 5173 端口的进程..." -ForegroundColor Yellow
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object {
    Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue
    Write-Host "  已停止进程 $_"
}
Start-Sleep -Seconds 2

if (-not (Test-Path (Join-Path $frontendDir "package.json"))) {
    Write-Host "未找到 frontend 目录或 package.json: $frontendDir" -ForegroundColor Red
    exit 1
}

Write-Host "正在启动前端开发服务 (端口 5173)..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendDir'; npm run dev"
Write-Host "前端已在新窗口启动，访问地址: http://localhost:5173/" -ForegroundColor Green
