@echo off
setlocal enabledelayedexpansion

REM ==========================================
REM Meal Planner Setup & Run Script (Windows)
REM ==========================================

echo [INFO] Starting Meal Planner setup script...
echo.

echo [INFO] Step 1: Checking for Java...
java -version >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo Please install Java 11 or higher.
    pause
    exit /b 1
)
echo [OK] Java found.

echo [INFO] Step 2: Checking for Maven...
set "MVN_CMD=mvn"
if exist "mvnw.cmd" (
    set "MVN_CMD=mvnw.cmd"
    echo [OK] Using Maven Wrapper.
) else (
    echo [OK] Will use Maven from PATH.
)

echo [INFO] Step 3: Checking dependencies...
if not exist "target\meal-planner-1.0-SNAPSHOT.jar" (
    echo [INFO] JAR file not found. Building project...
    echo [INFO] This may take a few minutes on first run...
    call "%MVN_CMD%" clean package -DskipTests
    if !errorlevel! neq 0 (
        echo [ERROR] Build failed. Please check the errors above.
        pause
        exit /b 1
    )
    echo [OK] Build completed successfully.
) else (
    echo [OK] JAR file found. Skipping build.
)

REM Verify JAR file exists after build
if not exist "target\meal-planner-1.0-SNAPSHOT.jar" (
    echo [ERROR] JAR file not found: target\meal-planner-1.0-SNAPSHOT.jar
    echo Build may have failed. Please check the errors above.
    pause
    exit /b 1
)

echo.
echo [INFO] Step 4: Starting Meal Planner...
echo.

REM Run the fat JAR created by maven-shade-plugin
REM Use start command to run in a separate window so the batch file doesn't block
echo [INFO] Launching application in a new window...

REM Check if Scenic View should be enabled (via environment variable)
if "%SCENICVIEW_ENABLE%"=="true" (
    echo [INFO] Scenic View debugging enabled.
    start "Meal Planner" java -Dscenicview.enable=true -cp "libs\scenicview.jar;target\meal-planner-1.0-SNAPSHOT.jar" com.mealplanner.app.Launcher
) else (
    start "Meal Planner" java -jar "target\meal-planner-1.0-SNAPSHOT.jar"
)

echo [INFO] Application launch command executed.
echo [INFO] The Meal Planner window should appear shortly.
echo [INFO] If the window does not appear, check for error messages above.
echo [INFO] You can close this window now.

