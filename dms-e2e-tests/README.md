# DMS End-to-End Tests

This module contains Selenium-based end-to-end tests for the Document Management System (DMS).

## Overview

The E2E test suite covers 90% of end-user use cases including:
- User authentication and authorization
- Document management operations (upload, download, list, update, delete)
- Admin operations (user, role, permission, application management)
- Compliance features (PCI-DSS, GDPR, ISO 27001)
- LLM query functionality

## Prerequisites

- Java 25 LTS
- Maven 3.8+
- Chrome or Firefox browser
- DMS services running (frontend and backend services)

## Configuration

### Environment Variables

Set the following system properties when running tests:

```bash
# Base URL for frontend
-Ddms.base.url=http://localhost:80

# Service URLs (optional, defaults provided)
-Ddms.admin.url=http://localhost:8081
-Ddms.document.url=http://localhost:8083
-Ddms.llm.url=http://localhost:8085
-Ddms.compliance.url=http://localhost:8084

# Browser selection (chrome or firefox)
-Dbrowser=chrome

# Headless mode
-Dheadless=false

# Selenium Grid Hub URL (optional, for remote execution)
-Dselenium.hub.url=http://localhost:4444/wd/hub
```

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=AuthenticationE2ETest
```

### Run with Specific Browser

```bash
mvn test -Dbrowser=firefox
```

### Run in Headless Mode

```bash
mvn test -Dheadless=true
```

### Run with Custom Base URL

```bash
mvn test -Ddms.base.url=http://staging.example.com
```

## Test Structure

```
e2e-tests/
├── src/test/java/com/davidparker/dms/e2e/
│   ├── base/
│   │   └── BaseE2ETest.java          # Base test class
│   ├── config/
│   │   └── TestConfig.java           # Test configuration
│   ├── pages/
│   │   ├── BasePage.java             # Base page object
│   │   ├── LoginPage.java            # Login page object
│   │   ├── DocumentListPage.java     # Document list page object
│   │   ├── AdminDashboardPage.java   # Admin dashboard page object
│   │   └── ComplianceDashboardPage.java # Compliance dashboard page object
│   └── tests/
│       ├── AuthenticationE2ETest.java
│       ├── DocumentManagementE2ETest.java
│       ├── AdminManagementE2ETest.java
│       ├── ComplianceE2ETest.java
│       └── LlmQueryE2ETest.java
```

## Test Coverage

### Authentication Tests
- ✅ Successful login with valid credentials
- ✅ Failed login with invalid credentials
- ✅ Login validation with empty credentials
- ✅ Unauthenticated access protection

### Document Management Tests
- ✅ View document list
- ✅ Filter documents by classification
- ✅ Upload new document
- ✅ View document details
- ✅ Download document
- ✅ Empty state handling

### Admin Management Tests
- ✅ Access admin dashboard
- ✅ Navigate to Users tab
- ✅ Navigate to Roles tab
- ✅ Navigate to Permissions tab
- ✅ Navigate to Applications tab
- ✅ Create new user
- ✅ Create new role
- ✅ Provision new application

### Compliance Tests
- ✅ Access compliance dashboard
- ✅ View PCI compliance report
- ✅ View GDPR compliance section
- ✅ View ISO 27001 controls
- ✅ Export data subject data
- ✅ Request data erasure

### LLM Query Tests
- ✅ Access LLM query interface
- ✅ Submit document query
- ✅ View query results
- ✅ Empty query validation

## Page Object Model

The tests use the Page Object Model (POM) pattern for maintainability:
- Each page has its own class with locators and methods
- Base page class provides common functionality
- Tests interact with pages, not directly with WebDriver

## Best Practices

1. **Wait Strategies**: Use explicit waits instead of Thread.sleep()
2. **Locators**: Prefer stable locators (ID, aria-label) over CSS/XPath
3. **Test Data**: Use test fixtures or data builders for test data
4. **Cleanup**: Tests clean up after themselves
5. **Isolation**: Each test is independent and can run in any order

## CI/CD Integration

Tests can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Run E2E Tests
  run: |
    mvn test -Dheadless=true -Ddms.base.url=${{ env.FRONTEND_URL }}
```

## Troubleshooting

### Tests Fail with Timeout
- Ensure all services are running
- Check network connectivity
- Verify base URLs are correct

### Browser Not Found
- WebDriverManager should auto-download drivers
- Manually install ChromeDriver or GeckoDriver if needed

### Element Not Found
- Check if page structure has changed
- Verify element locators are correct
- Increase wait timeout if page loads slowly

## Maintenance

When UI changes:
1. Update page object classes with new locators
2. Update test methods if workflows change
3. Run tests to verify they still pass
4. Update documentation if needed
