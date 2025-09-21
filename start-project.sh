#!/bin/bash

# NAMASTE-ICD11 Terminology Service Startup Script
# This script starts the complete project with all services

set -e

echo "🚀 Starting NAMASTE-ICD11 Terminology Service..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p logs
mkdir -p ssl
mkdir -p data

# Set permissions
chmod +x start-project.sh
chmod +x start-project.bat

# Stop any existing containers
echo "🛑 Stopping existing containers..."
docker-compose down

# Build and start services
echo "🔨 Building and starting services..."
docker-compose up --build -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# Check service health
echo "🏥 Checking service health..."

# Check PostgreSQL
if docker-compose exec -T postgres pg_isready -U terminology_user -d terminology_db > /dev/null 2>&1; then
    echo "✅ PostgreSQL is ready"
else
    echo "❌ PostgreSQL is not ready"
fi

# Check Redis
if docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; then
    echo "✅ Redis is ready"
else
    echo "❌ Redis is not ready"
fi

# Check Backend
if curl -f http://localhost:8080/fhir-terminology/actuator/health > /dev/null 2>&1; then
    echo "✅ Backend API is ready"
else
    echo "❌ Backend API is not ready"
fi

# Check Frontend
if curl -f http://localhost:3000 > /dev/null 2>&1; then
    echo "✅ Frontend is ready"
else
    echo "❌ Frontend is not ready"
fi

# Check Nginx
if curl -f http://localhost:80 > /dev/null 2>&1; then
    echo "✅ Nginx is ready"
else
    echo "❌ Nginx is not ready"
fi

echo ""
echo "🎉 NAMASTE-ICD11 Terminology Service is now running!"
echo ""
echo "📋 Service URLs:"
echo "   🌐 Frontend:        http://localhost:3000"
echo "   🔧 Backend API:     http://localhost:8080/fhir-terminology"
echo "   📚 Swagger UI:      http://localhost:8080/fhir-terminology/swagger-ui.html"
echo "   🏥 FHIR Metadata:   http://localhost:8080/fhir-terminology/fhir/metadata"
echo "   🔄 Nginx Proxy:     http://localhost:80"
echo ""
echo "📊 Database:"
echo "   🐘 PostgreSQL:      localhost:5432"
echo "   🔴 Redis:           localhost:6379"
echo ""
echo "📝 Logs:"
echo "   📄 View logs:       docker-compose logs -f"
echo "   🛑 Stop services:   docker-compose down"
echo "   🔄 Restart:         docker-compose restart"
echo ""
echo "🔐 Default Credentials:"
echo "   👤 Database User:   terminology_user"
echo "   🔑 Database Pass:   terminology_password"
echo "   🗄️  Database Name:   terminology_db"
echo ""
echo "✨ Happy coding with NAMASTE-ICD11!"