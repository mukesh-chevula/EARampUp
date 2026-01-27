#!/usr/bin/env bash

# Step-by-step setup and run helper for the gRPC Chat Application
# This script automates the most common tasks

set -e  # Exit on any error

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "=========================================="
echo "gRPC Chat Application - Complete Setup"
echo "=========================================="
echo ""

# Function to print colored output
print_step() {
    echo -e "\033[1;32m▶ $1\033[0m"
}

print_info() {
    echo -e "\033[1;34mℹ $1\033[0m"
}

print_error() {
    echo -e "\033[1;31m✗ $1\033[0m"
}

print_success() {
    echo -e "\033[1;32m✓ $1\033[0m"
}

# Check Java installation
check_java() {
    print_step "Checking Java installation..."
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed!"
        echo "Please install Java 17 or higher"
        echo "Visit: https://adoptopenjdk.net/"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | grep version | cut -d'"' -f2)
    print_success "Java $JAVA_VERSION is installed"
}

# Check Docker installation
check_docker() {
    print_step "Checking Docker installation..."
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed!"
        echo "Please install Docker: https://docs.docker.com/get-docker/"
        exit 1
    fi
    print_success "Docker is installed"
}

# Start Cassandra
start_cassandra() {
    print_step "Starting Cassandra container..."
    if ! docker-compose up -d cassandra 2>/dev/null; then
        print_error "Failed to start Cassandra"
        echo "Make sure Docker daemon is running"
        exit 1
    fi
    
    print_info "Waiting for Cassandra to be ready (40 seconds)..."
    sleep 40
    
    if docker-compose ps cassandra | grep -q "Up"; then
        print_success "Cassandra is running"
    else
        print_error "Cassandra failed to start"
        docker-compose logs cassandra | head -20
        exit 1
    fi
}

# Build project
build_project() {
    print_step "Building project..."
    print_info "This may take 1-2 minutes on first run..."
    
    if ./gradlew clean build > /dev/null 2>&1; then
        print_success "Project built successfully"
    else
        print_error "Build failed"
        ./gradlew clean build
        exit 1
    fi
}

# Summary
print_summary() {
    echo ""
    echo "=========================================="
    echo -e "\033[1;32m✓ Setup Complete!\033[0m"
    echo "=========================================="
    echo ""
    echo "Next steps:"
    echo ""
    echo "1. Start the server in a new terminal:"
    echo "   cd \"$PROJECT_DIR\""
    echo "   ./gradlew bootRun"
    echo ""
    echo "2. Start a client in another terminal:"
    echo "   cd \"$PROJECT_DIR\""
    echo "   ./gradlew run"
    echo ""
    echo "3. Click 'Connect' in the client GUI and start chatting!"
    echo ""
    echo "To send messages from server terminal:"
    echo "   Just type and press Enter"
    echo ""
    echo "To stop everything:"
    echo "   Press Ctrl+C in server terminal"
    echo "   Stop Cassandra: docker-compose down"
    echo ""
}

# Main execution
main() {
    check_java
    check_docker
    
    if [ "$1" == "--skip-cassandra" ]; then
        print_info "Skipping Cassandra startup..."
    else
        start_cassandra
    fi
    
    build_project
    
    print_summary
}

# Run main function
main "$@"
