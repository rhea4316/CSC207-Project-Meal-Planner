#!/bin/bash

# Simple run script for Meal Planner
# This script compiles and runs the application with proper JavaFX module configuration

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}[INFO]${NC} Compiling project..."
mvn clean compile

echo -e "${GREEN}[SUCCESS]${NC} Compilation complete!"
echo -e "${BLUE}[INFO]${NC} Starting Meal Planner..."
echo ""

# Run with explicit module configuration to avoid issues
java \
  --module-path $(mvn dependency:build-classpath -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout -q):target/classes \
  --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing \
  --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
  --enable-native-access=ALL-UNNAMED \
  -classpath target/classes:$(mvn dependency:build-classpath -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout -q) \
  com.mealplanner.app.Main
