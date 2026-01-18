#!/bin/bash

# DMS Requirements Verification Script
# Verifies that all project requirements are met

set -e

echo "=========================================="
echo "DMS Requirements Verification"
echo "=========================================="

ERRORS=0

# Check Java version
echo -n "Checking Java version... "
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" = "25" ]; then
    echo "✓ Java 25"
else
    echo "✗ Java version is $JAVA_VERSION, required: 25"
    ERRORS=$((ERRORS + 1))
fi

# Check Angular version in package.json
echo -n "Checking Angular version... "
ANGULAR_VERSION=$(grep -o '"@angular/core": "[^"]*"' dms-frontend-service/package.json | grep -o '[0-9]\+\.[0-9]\+\.[0-9]\+' | head -1 | cut -d'.' -f1)
if [ "$ANGULAR_VERSION" = "21" ]; then
    echo "✓ Angular 21"
else
    echo "✗ Angular version is $ANGULAR_VERSION, required: 21"
    ERRORS=$((ERRORS + 1))
fi

# Check OWASP plugin in all POMs
echo -n "Checking OWASP Dependency Check in POMs... "
POMS_WITH_OWASP=$(find . -name "pom.xml" -path "*/dms-*" -exec grep -l "dependency-check-maven" {} \; | wc -l)
if [ "$POMS_WITH_OWASP" -ge 5 ]; then
    echo "✓ OWASP configured in $POMS_WITH_OWASP services"
else
    echo "✗ OWASP not configured in all services"
    ERRORS=$((ERRORS + 1))
fi

# Check JaCoCo plugin in all POMs
echo -n "Checking JaCoCo in POMs... "
POMS_WITH_JACOCO=$(find . -name "pom.xml" -path "*/dms-*" -exec grep -l "jacoco-maven-plugin" {} \; | wc -l)
if [ "$POMS_WITH_JACOCO" -ge 5 ]; then
    echo "✓ JaCoCo configured in $POMS_WITH_JACOCO services"
else
    echo "✗ JaCoCo not configured in all services"
    ERRORS=$((ERRORS + 1))
fi

# Check npm audit configuration
echo -n "Checking npm audit configuration... "
if [ -f "dms-frontend-service/.npmrc" ] && grep -q "audit=true" dms-frontend-service/.npmrc; then
    echo "✓ npm audit configured"
else
    echo "✗ npm audit not configured"
    ERRORS=$((ERRORS + 1))
fi

# Check Flux configuration
echo -n "Checking Flux configuration... "
if [ -f "flux-config/gitrepository.yaml" ] && [ -f "flux-config/kustomization.yaml" ]; then
    echo "✓ Flux configuration present"
else
    echo "✗ Flux configuration missing"
    ERRORS=$((ERRORS + 1))
fi

# Check Istio configuration
echo -n "Checking Istio configuration... "
if [ -f "istio-config/virtual-service.yaml" ] && [ -f "istio-config/destination-rule.yaml" ]; then
    echo "✓ Istio configuration present"
else
    echo "✗ Istio configuration missing"
    ERRORS=$((ERRORS + 1))
fi

# Check .cursorrules
echo -n "Checking .cursorrules... "
if grep -q "Angular 21" .cursorrules && grep -q "Java 25" .cursorrules && grep -q "Azure AKS" .cursorrules; then
    echo "✓ .cursorrules updated"
else
    echo "✗ .cursorrules missing requirements"
    ERRORS=$((ERRORS + 1))
fi

echo "=========================================="
if [ $ERRORS -eq 0 ]; then
    echo "✓ All requirements verified!"
    exit 0
else
    echo "✗ Found $ERRORS requirement violations"
    exit 1
fi
