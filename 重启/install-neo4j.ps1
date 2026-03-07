# Neo4j 5.x 安装到 D:\图数据库
# 用法：在 PowerShell 中执行 .\install-neo4j.ps1
# 需 Java 17+（Neo4j 依赖）

$ErrorActionPreference = "Stop"
$InstallDir = "D:\图数据库"
$Neo4jVersion = "5.15.0"
$ZipName = "neo4j-community-$Neo4jVersion-windows.zip"
$DownloadUrl = "https://dist.neo4j.org/$ZipName"

if (-not (Test-Path $InstallDir)) {
    New-Item -ItemType Directory -Path $InstallDir -Force | Out-Null
    Write-Host "已创建目录: $InstallDir" -ForegroundColor Green
} else {
    Write-Host "安装目录已存在: $InstallDir" -ForegroundColor Cyan
}

$ZipPath = Join-Path $InstallDir $ZipName
$ExtractPath = Join-Path $InstallDir "neo4j-community-$Neo4jVersion"
$Neo4jBat = Join-Path $ExtractPath "bin\neo4j.bat"

# 强制重新安装时加参数 -Force；否则仅在缺少 neo4j.bat 时下载
$forceInstall = $args -contains "-Force"
$needInstall = $forceInstall -or -not (Test-Path -LiteralPath $Neo4jBat)

if ($needInstall) {
    Write-Host "正在下载 Neo4j $Neo4jVersion（约 80MB），请稍候..." -ForegroundColor Cyan
    try {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $DownloadUrl -OutFile $ZipPath -UseBasicParsing
    } catch {
        Write-Host "下载失败: $_" -ForegroundColor Red
        Write-Host "请手动从 https://neo4j.com/deployment-center/ 下载 Community 版 Windows (zip)，解压到: $InstallDir" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "正在解压到 $InstallDir ..." -ForegroundColor Cyan
    Expand-Archive -Path $ZipPath -DestinationPath $InstallDir -Force
    Remove-Item $ZipPath -Force -ErrorAction SilentlyContinue
    Write-Host "解压完成。" -ForegroundColor Green
} else {
    Write-Host "Neo4j $Neo4jVersion 已存在于 $ExtractPath，跳过下载。若需重装请加参数: .\install-neo4j.ps1 -Force" -ForegroundColor Yellow
}

$Neo4jHome = $ExtractPath
$BinPath = Join-Path $Neo4jHome "bin"
Write-Host ""
Write-Host "========== Neo4j 安装路径 ==========" -ForegroundColor Cyan
Write-Host "  NEO4J_HOME = $Neo4jHome"
Write-Host "  启动命令   = $BinPath\neo4j.bat console"
Write-Host ""
Write-Host "首次启动请设置初始密码（与 backend/application.yml 一致）：" -ForegroundColor Yellow
Write-Host "  用户名: neo4j  密码: neo4j123"
Write-Host "  或通过环境变量 NEO4J_PASSWORD 指定密码。" -ForegroundColor Gray
Write-Host ""
# 非交互模式（传入 -Start 参数）则直接启动
$startNow = $args -contains "-Start"
if (-not $startNow) {
    $ans = Read-Host "Start Neo4j console now? (Y/N)"
    $startNow = ($ans -eq 'Y' -or $ans -eq 'y')
}
if ($startNow) {
    Set-Location $BinPath
    & .\neo4j.bat console
}
