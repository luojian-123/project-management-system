# Neo4j 5.15 install to D:\neo4j (ASCII path to avoid encoding issues)
$ErrorActionPreference = "Stop"
$InstallDir = "D:\neo4j"
$Neo4jVersion = "5.15.0"
$ZipName = "neo4j-community-$Neo4jVersion-windows.zip"
$DownloadUrl = "https://dist.neo4j.org/$ZipName"

if (-not (Test-Path $InstallDir)) { New-Item -ItemType Directory -Path $InstallDir -Force | Out-Null }

$ZipPath = Join-Path $InstallDir $ZipName
$ExtractPath = Join-Path $InstallDir "neo4j-community-$Neo4jVersion"

Write-Host "Downloading Neo4j $Neo4jVersion ..."
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
Invoke-WebRequest -Uri $DownloadUrl -OutFile $ZipPath -UseBasicParsing

Write-Host "Extracting to $InstallDir ..."
Expand-Archive -Path $ZipPath -DestinationPath $InstallDir -Force
Remove-Item $ZipPath -Force -ErrorAction SilentlyContinue

Write-Host "Done. Neo4j at: $ExtractPath"
Write-Host "Start with: $ExtractPath\bin\neo4j.bat console"
