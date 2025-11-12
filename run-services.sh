#!/bin/bash

# IPO Microservices - Clean and Run Script
# This script demonstrates the complete microservices workflow

set -e  # Exit on any error

echo "🚀 Starting IPO Microservices System..."
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
}

# Clean all services
clean_services() {
    print_step "Cleaning all services..."
    for service in ipo-application-service ipo-payment-service ipo-allotment-service ipo-notification-service; do
        if [ -d "$service" ]; then
            cd "$service"
            if [ -d "target" ]; then
                print_status "Cleaning $service..."
                rm -rf target
            fi
            cd ..
        fi
    done
}

# Start infrastructure
start_infrastructure() {
    print_step "Starting infrastructure (PostgreSQL + ActiveMQ)..."
    docker-compose down -v 2>/dev/null || true
    docker-compose up -d

    # Wait for services to be ready
    print_status "Waiting for infrastructure to be ready..."
    sleep 20

    # Check if services are running
    if docker-compose ps | grep -q "Up"; then
        print_status "Infrastructure is running!"
    else
        print_error "Infrastructure failed to start"
        exit 1
    fi
}

# Build a service
build_service() {
    local service_name=$1
    local service_dir=$2

    print_step "Building $service_name..."
    cd "$service_dir"

    if [ ! -f "pom.xml" ]; then
        print_error "pom.xml not found in $service_dir"
        return 1
    fi

    # Build with Maven
    if mvn clean package -DskipTests -q; then
        print_status "$service_name built successfully"
    else
        print_error "Failed to build $service_name"
        return 1
    fi

    cd ..
}

# Start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local jar_file="$service_dir/target/$service_name-0.0.1-SNAPSHOT.jar"

    print_step "Starting $service_name..."

    if [ ! -f "$jar_file" ]; then
        print_error "JAR file not found: $jar_file"
        return 1
    fi

    # Start service in background
    java -jar "$jar_file" > "${service_name}.log" 2>&1 &
    local pid=$!

    # Store PID for later cleanup
    echo $pid > "${service_name}.pid"

    # Wait a bit for service to start
    sleep 15

    # Check if process is still running
    if kill -0 $pid 2>/dev/null; then
        print_status "$service_name started (PID: $pid)"
    else
        print_error "$service_name failed to start. Check ${service_name}.log for details"
        return 1
    fi
}

# Test basic connectivity
test_services() {
    print_step "Testing service connectivity..."

    # Test Application Service
    if curl -s http://localhost:8081/actuator/health > /dev/null; then
        print_status "Application Service (8081) is responding"
    else
        print_warning "Application Service (8081) is not responding yet"
    fi

    # Test Payment Service
    if curl -s http://localhost:8082/api/v1/payments/webhook -X POST -H "Content-Type: application/json" -d '{}' > /dev/null 2>&1; then
        print_status "Payment Service (8082) is responding"
    else
        print_warning "Payment Service (8082) is not responding yet"
    fi

    # Allotment and Notification services don't have REST endpoints
    print_status "Allotment Service (8083) - No REST API (scheduled tasks only)"
    print_status "Notification Service (8084) - No REST API (event-driven only)"
}

# Show usage instructions
show_instructions() {
    echo ""
    echo "🎉 IPO Microservices System is running!"
    echo "========================================"
    echo ""
    echo "Services running on:"
    echo "  📱 Application Service: http://localhost:8081"
    echo "  💳 Payment Service:     http://localhost:8082"
    echo "  🎯 Allotment Service:   http://localhost:8083 (scheduled)"
    echo "  📢 Notification Service: http://localhost:8084 (events)"
    echo ""
    echo "Test the APIs:"
    echo "  1. Import IPO_Microservices_API.postman_collection.json into Postman"
    echo "  2. Use the 'Complete Flow Test' folder to test end-to-end"
    echo ""
    echo "Manual testing:"
    echo "  # Submit application"
    echo "  curl -X POST http://localhost:8081/api/v1/ipo/testipo/apply \\"
    echo "    -H 'Content-Type: application/json' \\"
    echo "    -H 'Idempotency-Key: test123' \\"
    echo "    -d '{\"investorId\":\"user1\",\"lots\":5,\"userUpiId\":\"user@upi\"}'"
    echo ""
    echo "  # Approve payment (replace APP_ID with actual application ID)"
    echo "  curl -X POST http://localhost:8082/api/v1/payments/webhook \\"
    echo "    -H 'Content-Type: application/json' \\"
    echo "    -d '{\"applicationId\":\"APP_ID\",\"status\":\"APPROVED\",\"bankReferenceId\":\"BANK123\"}'"
    echo ""
    echo "Stop services: ./stop-services.sh"
    echo ""
}

# Cleanup function
cleanup() {
    print_step "Cleaning up..."
    # Kill any running service processes
    for pid_file in *.pid; do
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            if kill -0 $pid 2>/dev/null; then
                kill $pid
                print_status "Stopped process $pid"
            fi
            rm "$pid_file"
        fi
    done
}

# Trap for cleanup on script exit
trap cleanup EXIT

# Main execution
main() {
    print_status "IPO Microservices Clean and Run Script"
    print_status "======================================"

    # Check prerequisites
    check_docker

    # Clean everything
    clean_services

    # Start infrastructure
    start_infrastructure

    # Build all services
    print_step "Building all services..."
    build_service "ipo-application-service" "ipo-application-service"
    build_service "ipo-payment-service" "ipo-payment-service"
    build_service "ipo-allotment-service" "ipo-allotment-service"
    build_service "ipo-notification-service" "ipo-notification-service"

    # Start services in order (dependencies first)
    print_step "Starting services..."
    start_service "ipo-application-service" "ipo-application-service"
    start_service "ipo-payment-service" "ipo-payment-service"
    start_service "ipo-allotment-service" "ipo-allotment-service"
    start_service "ipo-notification-service" "ipo-notification-service"

    # Test connectivity
    test_services

    # Show instructions
    show_instructions

    print_status "System is ready! Press Ctrl+C to stop all services."

    # Wait for user interrupt
    wait
}

# Run main function
main "$@"