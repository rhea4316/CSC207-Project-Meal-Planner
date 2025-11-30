@echo off
REM ==========================================
REM Run Meal Planner with Scenic View
REM ==========================================

echo [INFO] Starting Meal Planner with Scenic View enabled...
echo.

set SCENICVIEW_ENABLE=true

echo [INFO] Building project...
call mvn clean package

if %errorlevel% neq 0 (
    echo [ERROR] Build failed. Please check the errors above.
    pause
    exit /b 1
)

echo [OK] Build successful!
echo [INFO] Launching application with Scenic View...
call setup_and_run.bat
