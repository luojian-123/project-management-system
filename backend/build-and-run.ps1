# 使用 D:\maven 编译并启动后端
$MAVEN = "D:\maven\bin\mvn.cmd"
$BackendDir = $PSScriptRoot

# 停止占用 8080 的进程
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object {
    Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue
}
Write-Host "已释放 8080 端口"

# 编译打包
Set-Location $BackendDir
& $MAVEN clean package -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# 启动
$jar = Join-Path $BackendDir "target\pms-backend-1.0.0.jar"
Write-Host "启动: java -jar $jar"
Start-Process java -ArgumentList "-jar", $jar -WorkingDirectory $BackendDir -NoNewWindow
Write-Host "后端已启动，接口地址: http://localhost:8080/api"
