@echo off
setlocal

REM ==========================================
REM Meal Planner Setup & Run Script (Windows)
REM ==========================================

echo [INFO] Checking for Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo Please install Java 11 or higher.
    pause
    exit /b 1
)

echo [INFO] Checking for Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [WARN] Maven is not in PATH.
    echo Attempting to use Maven Wrapper (mvnw)...
    
    if not exist "mvnw.cmd" (
        echo [ERROR] Maven Wrapper not found.
        echo Please install Maven or ensure mvnw is in the project root.
        pause
        exit /b 1
    )
    set MVN_CMD=mvnw.cmd
) else (
    set MVN_CMD=mvn
)

echo [INFO] Checking dependencies...
if not exist "target\meal-planner-1.0-SNAPSHOT.jar" (
    echo [INFO] Building project and installing dependencies...
    call %MVN_CMD% clean package
    if %errorlevel% neq 0 (
        echo [ERROR] Build failed. Please check the errors above.
        pause
        exit /b 1
    )
) else (
    echo [INFO] Project already built. Skipping build.
)

echo.
echo [INFO] Starting Meal Planner...
echo.

REM Run the fat JAR created by maven-shade-plugin
java -jar target\meal-planner-1.0-SNAPSHOT.jar

if %errorlevel% neq 0 (
    echo [ERROR] Application exited with error.
    pause
)

