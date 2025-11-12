#!/bin/bash

# IPO Microservices - Docker Compose Run Script
# This script builds and starts all services using Docker Compose

set -e  # Exit on any error

echo "🐳 Starting IPO Microservices with Docker Compose..."
echo "===================================================="

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
    print_status "Docker is running"
}

# Check if Docker Compose is available
check_docker_compose() {
    if ! docker compose version > /dev/null 2>&1; then
        print_error "Docker Compose is not available. Please install Docker Compose."
        exit 1
    fi
    print_status "Docker Compose is available"
}

# Stop and remove existing containers
cleanup_existing() {
    print_step "Cleaning up existing containers..."
    docker compose down -v 2>/dev/null || true
    print_status "Cleanup completed"
}

# Build and start services
start_services() {
    print_step "Building and starting services..."
    print_warning "This may take several minutes on first run..."
    
    if docker compose up --build -d; then
        print_status "All services started successfully"
    else
        print_error "Failed to start services"
        exit 1
    fi
}

# Wait for services to be healthy
wait_for_services() {
    print_step "Waiting for services to be healthy..."
    
    local max_wait=120
    local wait_time=0
    
    while [ $wait_time -lt $max_wait ]; do
        local healthy=$(docker compose ps --format json | grep -c '"Health":"healthy"' || echo 0)
        local total=$(docker compose ps --format json | wc -l)
        
        print_status "Healthy services: $healthy/$total"
        
        if [ "$healthy" -ge 4 ]; then
            print_status "Core infrastructure is healthy!"
            break
        fi
        
        sleep 5
        wait_time=$((wait_time + 5))
    done
    
    if [ $wait_time -ge $max_wait ]; then
        print_warning "Some services might not be fully healthy yet"
    fi
}

# Show service status
show_status() {
    print_step "Service Status:"
    docker compose ps
}

# Show usage instructions
show_instructions() {
    echo ""
    echo "🎉 IPO Microservices System is running!"
    echo "========================================"
    echo ""
    echo "Services available at:"
    echo "  🔧 Service Registry (Eureka): http://localhost:8761"
    echo "  🚪 API Gateway:               http://localhost:8080"
    echo "  📱 Application Service:       http://localhost:8081"
    echo "  💳 Payment Service:           http://localhost:8082"
    echo "  🎯 Allotment Service:         http://localhost:8087 (scheduled)"
    echo "  📢 Notification Service:      http://localhost:8084 (events)"
    echo ""
    echo "Infrastructure:"
    echo "  🐘 PostgreSQL:                localhost:5432"
    echo "  📨 ActiveMQ:                  localhost:61616 (web console: http://localhost:8161)"
    echo ""
    echo "Test the APIs through API Gateway:"
    echo "  # Submit IPO application"
    echo "  curl -X POST http://localhost:8080/api/v1/ipo/testipo/apply \\"
    echo "    -H 'Content-Type: application/json' \\"
    echo "    -H 'Idempotency-Key: test123' \\"
    echo "    -d '{\"investorId\":\"user1\",\"lots\":5,\"userUpiId\":\"user@upi\"}'"
    echo ""
    echo "  # Approve payment (replace APP_ID with actual application ID)"
    echo "  curl -X POST http://localhost:8080/api/v1/payments/webhook \\"
    echo "    -H 'Content-Type: application/json' \\"
    echo "    -d '{\"applicationId\":\"APP_ID\",\"status\":\"APPROVED\",\"bankReferenceId\":\"BANK123\"}'"
    echo ""
    echo "View logs:"
    echo "  docker compose logs -f [service-name]"
    echo ""
    echo "Stop services:"
    echo "  docker compose down"
    echo ""
    echo "Stop and remove volumes:"
    echo "  docker compose down -v"
    echo ""
}

# Main execution
main() {
    print_status "IPO Microservices Docker Compose Setup"
    print_status "======================================="
    
    # Check prerequisites
    check_docker
    check_docker_compose
    
    # Cleanup existing containers
    cleanup_existing
    
    # Start services
    start_services
    
    # Wait for services to be ready
    wait_for_services
    
    # Show status
    show_status
    
    # Show instructions
    show_instructions
    
    print_status "Setup complete! All services are running in Docker containers."
}

# Run main function
main "$@"
