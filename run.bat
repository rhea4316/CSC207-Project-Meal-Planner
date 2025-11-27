@echo off
setlocal EnableDelayedExpansion
REM JavaFX Meal Planner 실행 스크립트

set "MAVEN_REPO=%USERPROFILE%\.m2\repository"
set "JAVAFX_VERSION=17.0.2"
set "KOTLIN_VERSION=1.8.21"
set "OKIO_VERSION=3.6.0"

set "JAVAFX_CONTROLS_DIR=%MAVEN_REPO%\org\openjfx\javafx-controls\%JAVAFX_VERSION%"
set "JAVAFX_FXML_DIR=%MAVEN_REPO%\org\openjfx\javafx-fxml\%JAVAFX_VERSION%"
set "JAVAFX_BASE_DIR=%MAVEN_REPO%\org\openjfx\javafx-base\%JAVAFX_VERSION%"
set "JAVAFX_GRAPHICS_DIR=%MAVEN_REPO%\org\openjfx\javafx-graphics\%JAVAFX_VERSION%"
set "JAVAFX_MODULE_PATH=%JAVAFX_CONTROLS_DIR%;%JAVAFX_FXML_DIR%;%JAVAFX_BASE_DIR%;%JAVAFX_GRAPHICS_DIR%"

set "CLASSPATH=target\classes"
set "MISSING_DEPS="

echo ========================================
echo Meal Planner Application Launcher
echo ========================================
echo.
echo Building classpath...

call :AddJar "%JAVAFX_CONTROLS_DIR%\javafx-controls-%JAVAFX_VERSION%.jar"
call :AddJar "%JAVAFX_FXML_DIR%\javafx-fxml-%JAVAFX_VERSION%.jar"
call :AddJar "%JAVAFX_BASE_DIR%\javafx-base-%JAVAFX_VERSION%.jar"
call :AddJar "%JAVAFX_GRAPHICS_DIR%\javafx-graphics-%JAVAFX_VERSION%.jar"

call :AddJar "com\google\code\gson\gson\2.10.1\gson-2.10.1.jar"
call :AddJar "com\squareup\okhttp3\okhttp\4.12.0\okhttp-4.12.0.jar"
call :AddJar "org\json\json\20240303\json-20240303.jar"
call :AddJar "org\slf4j\slf4j-api\2.0.9\slf4j-api-2.0.9.jar"
call :AddJar "ch\qos\logback\logback-classic\1.4.11\logback-classic-1.4.11.jar"
call :AddJar "ch\qos\logback\logback-core\1.4.11\logback-core-1.4.11.jar"

echo   [INFO] Okio will be added: %OKIO_VERSION%
call :AddJar "com\squareup\okio\okio\%OKIO_VERSION%\okio-%OKIO_VERSION%.jar"

echo   [INFO] Kotlin stdlib will be added: %KOTLIN_VERSION%
call :AddJar "org\jetbrains\kotlin\kotlin-stdlib-jdk8\%KOTLIN_VERSION%\kotlin-stdlib-jdk8-%KOTLIN_VERSION%.jar"
call :AddJar "org\jetbrains\kotlin\kotlin-stdlib-jdk7\%KOTLIN_VERSION%\kotlin-stdlib-jdk7-%KOTLIN_VERSION%.jar"
call :AddJar "org\jetbrains\kotlin\kotlin-stdlib\%KOTLIN_VERSION%\kotlin-stdlib-%KOTLIN_VERSION%.jar"
call :AddJar "org\jetbrains\kotlin\kotlin-stdlib-common\%KOTLIN_VERSION%\kotlin-stdlib-common-%KOTLIN_VERSION%.jar"

if defined MISSING_DEPS (
    echo.
    echo 필수 의존성 JAR을 찾지 못했습니다. ^"mvn dependency:resolve^"를 실행한 뒤 다시 시도하세요.
    goto :wait
)

echo.
echo Starting Meal Planner Application...
echo Module Path: %JAVAFX_MODULE_PATH%
echo.

java --module-path "%JAVAFX_MODULE_PATH%" --add-modules javafx.controls,javafx.fxml -cp "%CLASSPATH%" com.mealplanner.app.Main
set "EXIT_CODE=%ERRORLEVEL%"
if not "%EXIT_CODE%"=="0" (
    echo.
    echo Application exited with error code: %EXIT_CODE%
)

:wait
echo.
pause
exit /b %EXIT_CODE%

:AddJar
set "REQUESTED=%~1"
if "%~d1"=="" (
    set "JAR_PATH=%MAVEN_REPO%\%REQUESTED%"
) else (
    set "JAR_PATH=%REQUESTED%"
)
if exist "%JAR_PATH%" (
    for %%F in ("%JAR_PATH%") do set "JAR_NAME=%%~nxF"
    set "CLASSPATH=%CLASSPATH%;%JAR_PATH%"
    echo   [OK] Added: !JAR_NAME!
) else (
    echo   [ERROR] Not found: %REQUESTED%
    set "MISSING_DEPS=1"
)
exit /b