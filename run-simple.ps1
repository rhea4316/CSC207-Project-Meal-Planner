# 간단한 실행 스크립트 - Maven exec 플러그인 사용
# Maven이 PATH에 있어야 합니다

Write-Host "Starting Meal Planner Application with Maven..."
Write-Host ""

# Maven exec 플러그인으로 실행
mvn exec:java -Dexec.mainClass="com.mealplanner.app.Main"

