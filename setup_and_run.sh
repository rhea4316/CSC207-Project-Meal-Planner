#!/bin/bash

# ==========================================
# Meal Planner Setup & Run Script (Mac/Linux)
# ==========================================

echo "[INFO] Checking for Java..."
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java is not installed or not in PATH."
    echo "Please install Java 11 or higher."
    exit 1
fi

echo "[INFO] Checking for Maven..."
if command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
elif [ -f "./mvnw" ]; then
    echo "[WARN] Maven is not in PATH. Using Maven Wrapper..."
    chmod +x ./mvnw
    MVN_CMD="./mvnw"
else
    echo "[ERROR] Maven not found. Please install Maven or include Maven Wrapper."
    exit 1
fi

echo "[INFO] Checking dependencies..."
if [ ! -f "target/meal-planner-1.0-SNAPSHOT.jar" ]; then
    echo "[INFO] Building project and installing dependencies..."
    $MVN_CMD clean package
    if [ $? -ne 0 ]; then
        echo "[ERROR] Build failed. Please check the errors above."
        exit 1
    fi
else
    echo "[INFO] Project already built. Skipping build."
fi

echo ""
echo "[INFO] Starting Meal Planner..."
echo ""

# Run the fat JAR created by maven-shade-plugin
java -jar target/meal-planner-1.0-SNAPSHOT.jar

