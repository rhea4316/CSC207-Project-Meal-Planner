#!/bin/bash

# ==========================================
# Meal Planner Setup & Run Script (Mac/Linux)
# ==========================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Detect OS
OS="$(uname -s)"
print_info "Detected OS: $OS"

# Check for Java
print_info "Checking for Java..."
if ! command -v java &> /dev/null; then
    print_error "Java is not installed or not in PATH."
    if [[ "$OS" == "Darwin" ]]; then
        print_info "On macOS, you can install Java using:"
        echo "  brew install openjdk@11"
        echo "  or download from: https://adoptium.net/"
    else
        echo "Please install Java 11 or higher."
    fi
    exit 1
fi

# Check Java version
JAVA_VERSION_OUTPUT=$(java -version 2>&1 | head -n 1)
print_info "Java version info: $JAVA_VERSION_OUTPUT"

# Extract major version number (handles both "1.8" and "11+" formats)
JAVA_MAJOR=$(java -version 2>&1 | head -n 1 | sed -E 's/.*version "([0-9]+)(\.[0-9]+)*.*/\1/')
if [ -z "$JAVA_MAJOR" ] || [ "$JAVA_MAJOR" -lt 11 ] 2>/dev/null; then
    print_error "Java 11 or higher is required. Found major version: $JAVA_MAJOR"
    exit 1
fi
print_success "Java found (major version: $JAVA_MAJOR)"

# Check for Maven
print_info "Checking for Maven..."
MVN_CMD=""

if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    print_success "Maven found: $MVN_VERSION"
    MVN_CMD="mvn"
elif [ -f "./mvnw" ]; then
    print_warn "Maven is not in PATH. Using Maven Wrapper..."
    chmod +x ./mvnw
    MVN_CMD="./mvnw"
    print_success "Maven Wrapper found and made executable"
else
    print_warn "Maven not found. Attempting to install..."
    
    if [[ "$OS" == "Darwin" ]]; then
        # Check for Homebrew
        if ! command -v brew &> /dev/null; then
            print_error "Homebrew is not installed."
            print_info "Installing Homebrew..."
            /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            
            # Add Homebrew to PATH for Apple Silicon Macs
            if [[ -f "/opt/homebrew/bin/brew" ]]; then
                eval "$(/opt/homebrew/bin/brew shellenv)"
            elif [[ -f "/usr/local/bin/brew" ]]; then
                eval "$(/usr/local/bin/brew shellenv)"
            fi
        fi
        
        print_info "Installing Maven using Homebrew..."
        brew install maven
        
        if command -v mvn &> /dev/null; then
            MVN_VERSION=$(mvn -version | head -n 1)
            print_success "Maven installed successfully: $MVN_VERSION"
            MVN_CMD="mvn"
        else
            print_error "Maven installation failed. Please install manually: brew install maven"
            exit 1
        fi
    else
        # Linux
        print_error "Maven not found. Please install Maven:"
        echo "  Ubuntu/Debian: sudo apt-get install maven"
        echo "  Fedora/RHEL: sudo dnf install maven"
        echo "  Or download from: https://maven.apache.org/download.cgi"
        exit 1
    fi
fi

if [ -z "$MVN_CMD" ]; then
    print_error "Could not determine Maven command."
    exit 1
fi

# Check dependencies and build
print_info "Checking dependencies..."
if [ ! -f "target/meal-planner-1.0-SNAPSHOT.jar" ]; then
    print_info "Building project and installing dependencies..."
    print_info "This may take a few minutes on first run..."
    print_info "Running: $MVN_CMD clean package -DskipTests"
    
    if $MVN_CMD clean package -DskipTests; then
        print_success "Build completed successfully!"
    else
        print_error "Build failed. Please check the errors above."
        exit 1
    fi
else
    print_info "Project already built. Skipping build."
fi

# Verify JAR file exists after build
if [ ! -f "target/meal-planner-1.0-SNAPSHOT.jar" ]; then
    print_error "JAR file not found: target/meal-planner-1.0-SNAPSHOT.jar"
    print_error "Build may have failed. Please check the errors above."
    exit 1
fi

print_success "JAR file found: target/meal-planner-1.0-SNAPSHOT.jar"

echo ""
print_info "Starting Meal Planner..."
echo ""

# Run the fat JAR created by maven-shade-plugin
if java -jar target/meal-planner-1.0-SNAPSHOT.jar; then
    print_success "Application exited successfully."
else
    EXIT_CODE=$?
    echo ""
    print_error "Application exited with error code: $EXIT_CODE"
    print_error "Please check the error messages above."
    exit $EXIT_CODE
fi

