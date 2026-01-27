#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}gRPC Chat Application - Setup & Run${NC}"
echo -e "${GREEN}========================================${NC}"

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        return 0  # Port is in use
    else
        return 1  # Port is not in use
    fi
}

# Function to start Cassandra
start_cassandra() {
    echo -e "${YELLOW}Starting Cassandra...${NC}"
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Docker is not installed. Please install Docker and Docker Compose.${NC}"
        exit 1
    fi
    
    docker-compose up -d cassandra
    
    echo -e "${YELLOW}Waiting for Cassandra to be ready...${NC}"
    sleep 10
    
    # Wait for health check
    for i in {1..30}; do
        if docker-compose ps cassandra | grep -q "healthy"; then
            echo -e "${GREEN}Cassandra is ready!${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
    done
    
    echo -e "${RED}Cassandra failed to start. Check logs with: docker-compose logs${NC}"
    exit 1
}

# Function to stop Cassandra
stop_cassandra() {
    echo -e "${YELLOW}Stopping Cassandra...${NC}"
    docker-compose down
    echo -e "${GREEN}Cassandra stopped.${NC}"
}

# Function to build the project
build_project() {
    echo -e "${YELLOW}Building the project...${NC}"
    ./gradlew clean build
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Build successful!${NC}"
    else
        echo -e "${RED}Build failed!${NC}"
        exit 1
    fi
}

# Function to run the server
run_server() {
    echo -e "${YELLOW}Starting gRPC Chat Server...${NC}"
    
    if check_port 9090; then
        echo -e "${RED}Port 9090 is already in use!${NC}"
        exit 1
    fi
    
    ./gradlew runServer
}

# Function to run a client
run_client() {
    echo -e "${YELLOW}Starting gRPC Chat Client...${NC}"
    ./gradlew runClient
}

# Main menu
show_menu() {
    echo ""
    echo -e "${GREEN}What would you like to do?${NC}"
    echo "1. Setup & Start Everything (Cassandra + Build)"
    echo "2. Build Project Only"
    echo "3. Start Server"
    echo "4. Start Client"
    echo "5. Start Cassandra Only"
    echo "6. Stop Cassandra"
    echo "7. Check Status"
    echo "8. Exit"
    echo ""
}

check_status() {
    echo -e "${YELLOW}Checking application status...${NC}"
    echo ""
    
    if docker-compose ps cassandra 2>/dev/null | grep -q "Up"; then
        echo -e "${GREEN}✓ Cassandra is running${NC}"
    else
        echo -e "${RED}✗ Cassandra is not running${NC}"
    fi
    
    if check_port 9090; then
        echo -e "${GREEN}✓ gRPC Server appears to be running on port 9090${NC}"
    else
        echo -e "${RED}✗ gRPC Server is not running${NC}"
    fi
    
    echo ""
}

# Main script
if [ "$#" -eq 0 ]; then
    # Interactive mode
    while true; do
        show_menu
        read -p "Enter your choice [1-8]: " choice
        
        case $choice in
            1)
                start_cassandra
                build_project
                echo -e "${GREEN}Setup complete! You can now:${NC}"
                echo "- Run: ./run.sh and select option 3 to start the server"
                echo "- Run: ./run.sh and select option 4 to start a client"
                ;;
            2)
                build_project
                ;;
            3)
                run_server
                ;;
            4)
                run_client
                ;;
            5)
                start_cassandra
                ;;
            6)
                stop_cassandra
                ;;
            7)
                check_status
                ;;
            8)
                echo -e "${GREEN}Goodbye!${NC}"
                exit 0
                ;;
            *)
                echo -e "${RED}Invalid option. Please try again.${NC}"
                ;;
        esac
    done
else
    # Command line mode
    case "$1" in
        setup)
            start_cassandra
            build_project
            ;;
        build)
            build_project
            ;;
        server)
            run_server
            ;;
        client)
            run_client
            ;;
        cassandra-start)
            start_cassandra
            ;;
        cassandra-stop)
            stop_cassandra
            ;;
        status)
            check_status
            ;;
        *)
            echo "Usage: $0 {setup|build|server|client|cassandra-start|cassandra-stop|status}"
            echo ""
            echo "Examples:"
            echo "  $0 setup          - Setup Cassandra and build project"
            echo "  $0 build          - Build project only"
            echo "  $0 server         - Run server"
            echo "  $0 client         - Run client"
            echo "  $0 cassandra-start - Start Cassandra"
            echo "  $0 cassandra-stop  - Stop Cassandra"
            echo "  $0 status         - Check application status"
            exit 1
            ;;
    esac
fi
