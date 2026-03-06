# Run org tables: create then fix. Prompts for MySQL password twice.
# Usage: cd D:\PMS系统代码\sql; .\run-org-tables.ps1

$dir = if ($PSScriptRoot) { $PSScriptRoot } else { Get-Location }
$f1 = (Join-Path $dir "V3_org_company_dept.sql") -replace '\\', '/'
$f2 = (Join-Path $dir "V3_org_fix_pm_dept.sql") -replace '\\', '/'

Write-Host "Step 1: Create pm_company / pm_dept ..." -ForegroundColor Cyan
& mysql -u root -p pms -e "source $f1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Create failed. Check MySQL and pms DB." -ForegroundColor Red
    exit 1
}

Write-Host "Step 2: Fix pm_dept.company_id (ignore Duplicate column) ..." -ForegroundColor Cyan
& mysql -u root -p pms -e "source $f2" 2>&1
Write-Host "Done. Refresh Org page." -ForegroundColor Green
