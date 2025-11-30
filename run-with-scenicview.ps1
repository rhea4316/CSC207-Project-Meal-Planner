# ==========================================
# Run Meal Planner with Scenic View
# ==========================================

Write-Host "[INFO] Starting Meal Planner with Scenic View enabled..." -ForegroundColor Cyan
Write-Host ""

# Enable Scenic View
$env:SCENICVIEW_ENABLE="true"

Write-Host "[INFO] Building project..." -ForegroundColor Yellow
mvn clean package

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Build successful!" -ForegroundColor Green
    Write-Host "[INFO] Launching application with Scenic View..." -ForegroundColor Cyan
    & $env:ComSpec /c "setup_and_run.bat"
} else {
    Write-Host "[ERROR] Build failed. Please check the errors above." -ForegroundColor Red
    exit 1
}
