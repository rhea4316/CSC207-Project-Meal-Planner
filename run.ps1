# JavaFX Meal Planner 실행 스크립트 (PowerShell)
# 이 스크립트는 모든 의존성을 포함하여 애플리케이션을 실행합니다.

$mavenRepo = "$env:USERPROFILE\.m2\repository"
$javafxVersion = "17.0.2"
$okioVersion = "3.6.0"

Write-Host "========================================"
Write-Host "Meal Planner Application Launcher"
Write-Host "========================================"
Write-Host ""

function Add-ClasspathJar {
    param(
        [Parameter(Mandatory = $true)]
        [string]$path
    )

    if ([System.IO.Path]::IsPathRooted($path)) {
        $jarPath = $path
    } else {
        $jarPath = Join-Path $mavenRepo $path
    }

    if (Test-Path $jarPath) {
        $script:classpath += ";$jarPath"
        Write-Host "  [OK] Added: $(Split-Path $jarPath -Leaf)"
    } else {
        Write-Host "  [ERROR] Not found: $path" -ForegroundColor Red
        $script:missingDeps += $path
    }
}

# JavaFX 모듈 경로 설정 (디렉토리 기반)
$javafxControlsDir = "$mavenRepo\org\openjfx\javafx-controls\$javafxVersion"
$javafxFxmlDir = "$mavenRepo\org\openjfx\javafx-fxml\$javafxVersion"
$javafxBaseDir = "$mavenRepo\org\openjfx\javafx-base\$javafxVersion"
$javafxGraphicsDir = "$mavenRepo\org\openjfx\javafx-graphics\$javafxVersion"

$modulePath = "$javafxControlsDir;$javafxFxmlDir;$javafxBaseDir;$javafxGraphicsDir"

# 클래스패스 설정
$classpath = "target\classes"
$missingDeps = @()

Write-Host "Building classpath..."

# JavaFX JAR 파일들을 클래스패스에 추가
$javafxJars = @(
    "$javafxControlsDir\javafx-controls-$javafxVersion.jar",
    "$javafxFxmlDir\javafx-fxml-$javafxVersion.jar",
    "$javafxBaseDir\javafx-base-$javafxVersion.jar",
    "$javafxGraphicsDir\javafx-graphics-$javafxVersion.jar"
)

foreach ($jar in $javafxJars) {
    Add-ClasspathJar -path $jar
}

# 필요한 의존성 JAR 파일들 찾기
$dependencies = @(
    "com\google\code\gson\gson\2.10.1\gson-2.10.1.jar",
    "com\squareup\okhttp3\okhttp\4.12.0\okhttp-4.12.0.jar",
    "org\json\json\20240303\json-20240303.jar",
    "org\slf4j\slf4j-api\2.0.9\slf4j-api-2.0.9.jar",
    "ch\qos\logback\logback-classic\1.4.11\logback-classic-1.4.11.jar",
    "ch\qos\logback\logback-core\1.4.11\logback-core-1.4.11.jar",
    "com\squareup\okio\okio\$okioVersion\okio-$okioVersion.jar"
)

Write-Host "  [INFO] Okio will be added: $okioVersion" -ForegroundColor Cyan

foreach ($dep in $dependencies) {
    Add-ClasspathJar -path $dep
}

# OkHttp는 Kotlin으로 작성되어 있어 Kotlin 표준 라이브러리가 필요함
# OkHttp 4.12.0은 kotlin-stdlib-jdk8 1.8.21을 사용함
$kotlinVersion = "1.8.21"
$kotlinDeps = @(
    "org\jetbrains\kotlin\kotlin-stdlib-jdk8\$kotlinVersion\kotlin-stdlib-jdk8-$kotlinVersion.jar",
    "org\jetbrains\kotlin\kotlin-stdlib-jdk7\$kotlinVersion\kotlin-stdlib-jdk7-$kotlinVersion.jar",
    "org\jetbrains\kotlin\kotlin-stdlib\$kotlinVersion\kotlin-stdlib-$kotlinVersion.jar",
    "org\jetbrains\kotlin\kotlin-stdlib-common\$kotlinVersion\kotlin-stdlib-common-$kotlinVersion.jar"
)
Write-Host "  [INFO] Kotlin stdlib will be added: $kotlinVersion" -ForegroundColor Cyan

foreach ($kotlinDep in $kotlinDeps) {
    Add-ClasspathJar -path $kotlinDep
}

if ($missingDeps.Count -gt 0) {
    Write-Host ""
    Write-Host "필수 의존성 JAR을 찾지 못해 실행을 중단합니다." -ForegroundColor Red
    Write-Host "다음 경로를 확인하거나 'mvn dependency:resolve'를 실행하세요:" -ForegroundColor Yellow
    foreach ($dep in $missingDeps) {
        Write-Host "   - $dep"
    }
    Write-Host ""
    Write-Host "Press any key to exit..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    return
}

Write-Host ""
Write-Host "Starting Meal Planner Application..."
Write-Host "Module Path: $modulePath"
Write-Host ""

# JavaFX 애플리케이션 실행
try {
    java --module-path $modulePath --add-modules javafx.controls,javafx.fxml -cp $classpath com.mealplanner.app.Main
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "Application exited with error code: $LASTEXITCODE" -ForegroundColor Red
        Write-Host "Please check the error messages above." -ForegroundColor Yellow
    }
} catch {
    Write-Host ""
    Write-Host "Error launching application: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Trying alternative method (classpath only)..." -ForegroundColor Yellow
    java -cp $classpath com.mealplanner.app.Main
}

Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
