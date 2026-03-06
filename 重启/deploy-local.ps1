# 本地一键部署并启动前后端
# 用法：在 D:\PMS系统代码\重启 下执行 .\deploy-local.ps1

$ErrorActionPreference = "Stop"

$root = Split-Path $PSScriptRoot -Parent
$backendDir = Join-Path $root "backend"
$frontendDir = Join-Path $root "frontend"
$backendJar = Join-Path $backendDir "target\pms-backend-1.0.0.jar"

function Stop-PortProcess($port) {
    Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique |
        ForEach-Object {
            Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue
            Write-Host "已停止端口 $port 对应进程: $_" -ForegroundColor Yellow
        }
}

if (-not (Test-Path (Join-Path $backendDir "pom.xml"))) {
    Write-Host "未找到后端目录或 pom.xml: $backendDir" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path (Join-Path $frontendDir "package.json"))) {
    Write-Host "未找到前端目录或 package.json: $frontendDir" -ForegroundColor Red
    exit 1
}

Write-Host "========== 1. 停止旧服务 ==========" -ForegroundColor Cyan
Stop-PortProcess 8080
Stop-PortProcess 5173
Start-Sleep -Seconds 2

Write-Host "========== 2. 构建后端 ==========" -ForegroundColor Cyan
Set-Location $backendDir
mvn clean package -DskipTests

if (-not (Test-Path $backendJar)) {
    Write-Host "后端构建完成但未找到 jar: $backendJar" -ForegroundColor Red
    exit 1
}

Write-Host "========== 3. 安装前端依赖 ==========" -ForegroundColor Cyan
Set-Location $frontendDir
if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
    npm install
} else {
    Write-Host "已检测到 node_modules，跳过 npm install" -ForegroundColor Yellow
}

Write-Host "========== 4. 启动后端 ==========" -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendDir'; java -jar target\pms-backend-1.0.0.jar"
Write-Host "后端已启动 -> http://localhost:8080/api" -ForegroundColor Green

Write-Host "========== 5. 启动前端 ==========" -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendDir'; npm run dev"
Write-Host "前端已启动 -> http://localhost:5173/" -ForegroundColor Green

Write-Host ""
Write-Host "本地一键部署完成。" -ForegroundColor Cyan
Write-Host "若需局域网地址，可执行: .\生成局域网链接.ps1" -ForegroundColor Gray
