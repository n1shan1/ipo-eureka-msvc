#!/bin/bash

# IPO Microservices - Stop Script
# Stops all running services and infrastructure

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

echo "🛑 Stopping IPO Microservices System..."
echo "======================================="

# Stop service processes
print_step "Stopping service processes..."
for pid_file in *.pid; do
    if [ -f "$pid_file" ]; then
        service_name=$(basename "$pid_file" .pid)
        pid=$(cat "$pid_file")
        if kill -0 $pid 2>/dev/null; then
            print_status "Stopping $service_name (PID: $pid)..."
            kill $pid
            # Wait for process to stop
            sleep 2
            if kill -0 $pid 2>/dev/null; then
                print_warning "Force killing $service_name..."
                kill -9 $pid
            fi
        else
            print_warning "$service_name (PID: $pid) was not running"
        fi
        rm "$pid_file"
    fi
done

# Stop infrastructure
print_step "Stopping infrastructure..."
if command -v docker-compose &> /dev/null; then
    docker-compose down -v
    print_status "Infrastructure stopped"
else
    print_warning "docker-compose not found, skipping infrastructure cleanup"
fi

# Clean up log files
print_step "Cleaning up log files..."
rm -f *.log

print_status "All services stopped and cleaned up!"
echo ""
echo "To restart: ./run-services.sh"