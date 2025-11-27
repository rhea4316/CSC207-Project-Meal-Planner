@echo off
REM 간단한 실행 스크립트 - Maven exec 플러그인 사용

echo Starting Meal Planner Application with Maven...
echo.

mvn exec:java -Dexec.mainClass="com.mealplanner.app.Main"

pause

