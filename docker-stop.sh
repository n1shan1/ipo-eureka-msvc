#!/bin/bash

# IPO Microservices - Docker Compose Stop Script
# This script stops all Docker containers

echo "🛑 Stopping IPO Microservices..."
echo "================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "⚠️  Docker is not running."
    exit 1
fi

# Stop and remove containers
echo "📦 Stopping containers..."
docker compose down

echo ""
echo "✅ All containers stopped!"
echo ""
echo "To also remove volumes (database data), run:"
echo "  docker compose down -v"
echo ""
echo "To view stopped containers:"
echo "  docker compose ps -a"
echo ""
