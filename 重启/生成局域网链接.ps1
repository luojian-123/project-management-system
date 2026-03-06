# 生成项目管理系统局域网访问链接
# 用法：在 D:\PMS系统代码\重启 下执行 .\生成局域网链接.ps1

$ErrorActionPreference = "SilentlyContinue"

# 获取本机局域网 IP（排除回环、虚拟网卡）
$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.InterfaceAlias -notmatch "Loopback|Virtual|VMware|Vbox|Docker|WSL" -and
    $_.IPAddress -notmatch "^127\.|^169\."
} | Select-Object -First 1).IPAddress

if (-not $ip) {
    $ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notmatch "^127\." } | Select-Object -First 1).IPAddress
}
if (-not $ip) {
    $ip = "本机IP"
}

$frontendPort = 5173
$backendPort = 8080

$frontendUrl = "http://${ip}:${frontendPort}/"
$backendUrl  = "http://${ip}:${backendPort}/api"

Write-Host ""
Write-Host "========== 项目管理系统 - 局域网访问链接 ==========" -ForegroundColor Cyan
Write-Host ""
Write-Host "  前端（浏览器访问）: " -NoNewline
Write-Host $frontendUrl -ForegroundColor Green
Write-Host "  后端 API:          " -NoNewline
Write-Host $backendUrl -ForegroundColor Green
Write-Host ""
Write-Host "  本机 IP: $ip" -ForegroundColor Yellow
Write-Host "  说明: 同一局域网内的设备可用上述链接访问（需先运行 重启\restart-all.ps1 启动前后端）" -ForegroundColor Gray
Write-Host ""

try {
    $frontendUrl | Set-Clipboard
    Write-Host "  (Frontend URL copied to clipboard)" -ForegroundColor Gray
} catch { }
Write-Host ""
