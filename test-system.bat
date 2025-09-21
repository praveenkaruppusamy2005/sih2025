@echo off
REM NAMASTE-ICD11 Terminology Service Test Script for Windows
REM This script tests the complete system functionality

echo 🧪 Testing NAMASTE-ICD11 Terminology Service...

REM Wait for services to be ready
echo ⏳ Waiting for services to be ready...
timeout /t 10 /nobreak >nul

echo.
echo 🔍 Running System Tests...
echo ==========================

set /a total_tests=0
set /a passed_tests=0

REM Test 1: Backend Health Check
set /a total_tests+=1
echo Testing Backend Health Check...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/fhir-terminology/actuator/health > temp_response.txt 2>nul
if %errorlevel% equ 0 (
    set /p response=<temp_response.txt
    if "!response!"=="200" (
        echo ✅ PASS (HTTP !response!)
        set /a passed_tests+=1
    ) else (
        echo ❌ FAIL (Expected HTTP 200, got HTTP !response!)
    )
) else (
    echo ❌ FAIL (Connection error)
)
del temp_response.txt 2>nul

REM Test 2: Frontend Health Check
set /a total_tests+=1
echo Testing Frontend Health Check...
curl -s -o nul -w "%%{http_code}" http://localhost:3000 > temp_response.txt 2>nul
if %errorlevel% equ 0 (
    set /p response=<temp_response.txt
    if "!response!"=="200" (
        echo ✅ PASS (HTTP !response!)
        set /a passed_tests+=1
    ) else (
        echo ❌ FAIL (Expected HTTP 200, got HTTP !response!)
    )
) else (
    echo ❌ FAIL (Connection error)
)
del temp_response.txt 2>nul

REM Test 3: FHIR Metadata
set /a total_tests+=1
echo Testing FHIR Metadata...
curl -s http://localhost:8080/fhir-terminology/fhir/metadata > temp_response.json 2>nul
if %errorlevel% equ 0 (
    findstr /C:"resourceType" temp_response.json >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ PASS (Valid JSON with resourceType)
        set /a passed_tests+=1
    ) else (
        echo ❌ FAIL (Invalid JSON or missing resourceType)
    )
) else (
    echo ❌ FAIL (Connection error)
)
del temp_response.json 2>nul

REM Test 4: NAMASTE CodeSystem
set /a total_tests+=1
echo Testing NAMASTE CodeSystem...
curl -s http://localhost:8080/fhir-terminology/fhir/CodeSystem/namaste-codes > temp_response.json 2>nul
if %errorlevel% equ 0 (
    findstr /C:"resourceType" temp_response.json >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ PASS (Valid JSON with resourceType)
        set /a passed_tests+=1
    ) else (
        echo ❌ FAIL (Invalid JSON or missing resourceType)
    )
) else (
    echo ❌ FAIL (Connection error)
)
del temp_response.json 2>nul

REM Test 5: System Statistics
set /a total_tests+=1
echo Testing System Statistics...
curl -s http://localhost:8080/fhir-terminology/api/terminology/stats > temp_response.json 2>nul
if %errorlevel% equ 0 (
    findstr /C:"namasteCodeCount" temp_response.json >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ PASS (Valid JSON with namasteCodeCount)
        set /a passed_tests+=1
    ) else (
        echo ❌ FAIL (Invalid JSON or missing namasteCodeCount)
    )
) else (
    echo ❌ FAIL (Connection error)
)
del temp_response.json 2>nul

REM Test 6: Swagger UI
set /a total_tests+=1
echo Testing Swagger UI...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/fhir-terminology/swagger-ui.html > temp_response.txt 2>nul
if %errorlevel% equ 0 (
    set /p response=<temp_response.txt
    if "!response!"=="200" (
        echo ✅ PASS (HTTP !response!)
        set /a passed_tests+=1
    ) else (
        echo ❌ FAIL (Expected HTTP 200, got HTTP !response!)
    )
) else (
    echo ❌ FAIL (Connection error)
)
del temp_response.txt 2>nul

echo.
echo 📊 Test Results
echo ===============
echo Total Tests: %total_tests%
echo Passed: %passed_tests%
set /a failed_tests=%total_tests%-%passed_tests%
echo Failed: %failed_tests%

if %passed_tests% equ %total_tests% (
    echo 🎉 All tests passed! System is working correctly.
    exit /b 0
) else (
    echo ❌ Some tests failed. Please check the system configuration.
    exit /b 1
)

pause
