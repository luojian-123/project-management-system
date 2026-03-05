# 刷新 PATH 并验证 Maven（在 Cursor 终端中运行此脚本即可）
$env:Path = [Environment]::GetEnvironmentVariable("Path", "User") + ";" + [Environment]::GetEnvironmentVariable("Path", "Machine")
Write-Host "执行: mvn -version" -ForegroundColor Cyan
mvn -version
