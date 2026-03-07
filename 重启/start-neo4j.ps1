# 启动已安装在 D:\neo4j 下的 Neo4j（控制台模式）
# 安装请先执行 .\install-neo4j-simple.ps1

$InstallDir = "D:\neo4j"
$Neo4jVersion = "5.15.0"
$Neo4jHome = Join-Path $InstallDir "neo4j-community-$Neo4jVersion"
$BinPath = Join-Path $Neo4jHome "bin"

if (-not (Test-Path (Join-Path $BinPath "neo4j.bat"))) {
    Write-Host "Neo4j not found. Run .\install-neo4j-simple.ps1 to install to D:\neo4j" -ForegroundColor Red
    exit 1
}

Set-Location $BinPath
& .\neo4j.bat console
