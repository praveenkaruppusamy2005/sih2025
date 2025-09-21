#!/bin/bash

# NAMASTE-ICD11 Terminology Service Test Script
# This script tests the complete system functionality

set -e

echo "🧪 Testing NAMASTE-ICD11 Terminology Service..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test functions
test_endpoint() {
    local url=$1
    local description=$2
    local expected_status=${3:-200}
    
    echo -n "Testing $description... "
    
    if response=$(curl -s -w "%{http_code}" -o /dev/null "$url" 2>/dev/null); then
        if [ "$response" = "$expected_status" ]; then
            echo -e "${GREEN}✅ PASS${NC} (HTTP $response)"
            return 0
        else
            echo -e "${RED}❌ FAIL${NC} (Expected HTTP $expected_status, got HTTP $response)"
            return 1
        fi
    else
        echo -e "${RED}❌ FAIL${NC} (Connection error)"
        return 1
    fi
}

test_json_endpoint() {
    local url=$1
    local description=$2
    local expected_field=$3
    
    echo -n "Testing $description... "
    
    if response=$(curl -s "$url" 2>/dev/null); then
        if echo "$response" | jq -e ".$expected_field" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ PASS${NC} (Valid JSON with $expected_field)"
            return 0
        else
            echo -e "${RED}❌ FAIL${NC} (Invalid JSON or missing $expected_field)"
            return 1
        fi
    else
        echo -e "${RED}❌ FAIL${NC} (Connection error)"
        return 1
    fi
}

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Test counter
total_tests=0
passed_tests=0

echo ""
echo "🔍 Running System Tests..."
echo "=========================="

# Test 1: Backend Health Check
total_tests=$((total_tests + 1))
if test_endpoint "http://localhost:8080/fhir-terminology/actuator/health" "Backend Health Check"; then
    passed_tests=$((passed_tests + 1))
fi

# Test 2: Frontend Health Check
total_tests=$((total_tests + 1))
if test_endpoint "http://localhost:3000" "Frontend Health Check"; then
    passed_tests=$((passed_tests + 1))
fi

# Test 3: FHIR Metadata
total_tests=$((total_tests + 1))
if test_json_endpoint "http://localhost:8080/fhir-terminology/fhir/metadata" "FHIR Metadata" "resourceType"; then
    passed_tests=$((passed_tests + 1))
fi

# Test 4: NAMASTE CodeSystem
total_tests=$((total_tests + 1))
if test_json_endpoint "http://localhost:8080/fhir-terminology/fhir/CodeSystem/namaste-codes" "NAMASTE CodeSystem" "resourceType"; then
    passed_tests=$((passed_tests + 1))
fi

# Test 5: ConceptMap
total_tests=$((total_tests + 1))
if test_json_endpoint "http://localhost:8080/fhir-terminology/fhir/ConceptMap/namaste-to-icd11" "ConceptMap" "resourceType"; then
    passed_tests=$((passed_tests + 1))
fi

# Test 6: NAMASTE Search API
total_tests=$((total_tests + 1))
if test_json_endpoint "http://localhost:8080/fhir-terminology/api/terminology/namaste/search?term=test" "NAMASTE Search API" "content"; then
    passed_tests=$((passed_tests + 1))
fi

# Test 7: ICD-11 Search API
total_tests=$((total_tests + 1))
if test_json_endpoint "http://localhost:8080/fhir-terminology/api/terminology/icd11/search?term=test" "ICD-11 Search API" "content"; then
    passed_tests=$((passed_tests + 1))
fi

# Test 8: System Statistics
total_tests=$((total_tests + 1))
if test_json_endpoint "http://localhost:8080/fhir-terminology/api/terminology/stats" "System Statistics" "namasteCodeCount"; then
    passed_tests=$((passed_tests + 1))
fi

# Test 9: Swagger UI
total_tests=$((total_tests + 1))
if test_endpoint "http://localhost:8080/fhir-terminology/swagger-ui.html" "Swagger UI" 200; then
    passed_tests=$((passed_tests + 1))
fi

# Test 10: Nginx Proxy
total_tests=$((total_tests + 1))
if test_endpoint "http://localhost:80" "Nginx Proxy" 200; then
    passed_tests=$((passed_tests + 1))
fi

echo ""
echo "📊 Test Results"
echo "==============="
echo "Total Tests: $total_tests"
echo "Passed: $passed_tests"
echo "Failed: $((total_tests - passed_tests))"

if [ $passed_tests -eq $total_tests ]; then
    echo -e "${GREEN}🎉 All tests passed! System is working correctly.${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please check the system configuration.${NC}"
    exit 1
fi
