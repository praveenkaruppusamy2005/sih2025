@echo off
REM NAMASTE-ICD11 Terminology Service Startup Script for Windows
REM This script starts the complete project with all services

echo 🚀 Starting NAMASTE-ICD11 Terminology Service...

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not installed. Please install Docker Desktop first.
    pause
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose is not installed. Please install Docker Compose first.
    pause
    exit /b 1
)

REM Create necessary directories
echo 📁 Creating necessary directories...
if not exist logs mkdir logs
if not exist ssl mkdir ssl
if not exist data mkdir data

REM Stop any existing containers
echo 🛑 Stopping existing containers...
docker-compose down

REM Build and start services
echo 🔨 Building and starting services...
docker-compose up --build -d

REM Wait for services to be ready
echo ⏳ Waiting for services to be ready...
timeout /t 30 /nobreak >nul

REM Check service health
echo 🏥 Checking service health...

REM Check PostgreSQL
docker-compose exec -T postgres pg_isready -U terminology_user -d terminology_db >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ PostgreSQL is ready
) else (
    echo ❌ PostgreSQL is not ready
)

REM Check Redis
docker-compose exec -T redis redis-cli ping >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Redis is ready
) else (
    echo ❌ Redis is not ready
)

REM Check Backend
curl -f http://localhost:8080/fhir-terminology/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Backend API is ready
) else (
    echo ❌ Backend API is not ready
)

REM Check Frontend
curl -f http://localhost:3000 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Frontend is ready
) else (
    echo ❌ Frontend is not ready
)

REM Check Nginx
curl -f http://localhost:80 >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Nginx is ready
) else (
    echo ❌ Nginx is not ready
)

echo.
echo 🎉 NAMASTE-ICD11 Terminology Service is now running!
echo.
echo 📋 Service URLs:
echo    🌐 Frontend:        http://localhost:3000
echo    🔧 Backend API:     http://localhost:8080/fhir-terminology
echo    📚 Swagger UI:      http://localhost:8080/fhir-terminology/swagger-ui.html
echo    🏥 FHIR Metadata:   http://localhost:8080/fhir-terminology/fhir/metadata
echo    🔄 Nginx Proxy:     http://localhost:80
echo.
echo 📊 Database:
echo    🐘 PostgreSQL:      localhost:5432
echo    🔴 Redis:           localhost:6379
echo.
echo 📝 Logs:
echo    📄 View logs:       docker-compose logs -f
echo    🛑 Stop services:   docker-compose down
echo    🔄 Restart:         docker-compose restart
echo.
echo 🔐 Default Credentials:
echo    👤 Database User:   terminology_user
echo    🔑 Database Pass:   terminology_password
echo    🗄️  Database Name:   terminology_db
echo.
echo ✨ Happy coding with NAMASTE-ICD11!
echo.
pause