#!/bin/bash

# DMS Local Development Setup Script
# This script sets up the local development environment

set -e

echo "🚀 Setting up DMS Local Development Environment..."

# Check prerequisites
echo "📋 Checking prerequisites..."
command -v docker >/dev/null 2>&1 || { echo "❌ Docker is required but not installed. Aborting." >&2; exit 1; }
command -v docker-compose >/dev/null 2>&1 || { echo "❌ Docker Compose is required but not installed. Aborting." >&2; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "⚠️  Maven not found. Some services may need manual building." >&2; }
command -v npm >/dev/null 2>&1 || { echo "⚠️  npm not found. Frontend service may need manual building." >&2; }

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cat > .env << EOF
# DMS Local Development Environment Variables
ENVIRONMENT=dev
POSTGRES_PASSWORD=postgres
REDIS_PASSWORD=
EOF
    echo "✅ Created .env file. Please update with your Azure credentials if needed."
fi

# Build and start services
echo "🔨 Building Docker images..."
docker-compose build

echo "🚀 Starting services..."
docker-compose up -d

echo "⏳ Waiting for services to be healthy..."
sleep 30

# Check service health
echo "🏥 Checking service health..."
docker-compose ps

echo ""
echo "✅ Local development environment is ready!"
echo ""
echo "Services available at:"
echo "  - Frontend: http://localhost:80"
echo "  - Admin Service: http://localhost:8081"
echo "  - Audit Service: http://localhost:8082"
echo "  - Document Service: http://localhost:8083"
echo "  - Compliance Service: http://localhost:8084"
echo "  - LLM Service: http://localhost:8085"
echo ""
echo "Databases:"
echo "  - Admin DB: localhost:5432"
echo "  - Document DB: localhost:5433"
echo "  - Audit DB: localhost:5434"
echo "  - Redis: localhost:6379"
echo ""
echo "To view logs: docker-compose logs -f [service-name]"
echo "To stop services: docker-compose down"
