# DMS Test Coverage Plan

## Goal: 90% Code Coverage for All Services

### Test Coverage Requirements

Each service must achieve **minimum 90% code coverage** with comprehensive unit tests covering:

1. **Controllers** - All REST endpoints
2. **Services** - All business logic methods
3. **Repositories** - Data access layer (if applicable)
4. **Exception Handlers** - Error handling scenarios
5. **Security Configurations** - Authentication and authorization
6. **DTOs** - Validation logic

### Test Framework

- **JUnit 5** - Test framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **Spring Security Test** - Security testing
- **JaCoCo** - Code coverage reporting

### Coverage Tools

JaCoCo Maven plugin configured in all service POMs:
- Generates coverage reports in `target/site/jacoco/`
- Enforces 90% minimum coverage threshold
- Fails build if coverage is below threshold

## Test Structure by Service

### dms-admin-service ✅ IN PROGRESS

**Completed Tests:**
- ✅ UserManagementServiceTest - All CRUD operations
- ✅ RoleManagementServiceTest - All CRUD operations
- ✅ PermissionManagementServiceTest - All CRUD operations
- ✅ ApplicationManagementServiceTest - Provision and management
- ✅ UserManagementControllerTest - All endpoints
- ✅ GlobalExceptionHandlerTest - Error handling

**Remaining Tests:**
- ⏳ RoleManagementControllerTest
- ⏳ PermissionManagementControllerTest
- ⏳ ApplicationManagementControllerTest
- ⏳ AdminControllerTest
- ⏳ SecurityConfigTest
- ⏳ CorsConfigTest
- ⏳ Repository tests (if needed)

### dms-document-service ⏳ TODO

**Required Tests:**
- ⏳ DocumentServiceTest
- ⏳ DocumentControllerTest
- ⏳ DocumentVersionServiceTest
- ⏳ ApplicationServiceClientTest
- ⏳ AuditEventClientTest
- ⏳ SecurityConfigTest
- ⏳ Repository tests

### dms-audit-service ⏳ TODO

**Required Tests:**
- ⏳ AuditServiceTest
- ⏳ AuditControllerTest
- ⏳ SecurityConfigTest
- ⏳ Repository tests

### dms-compliance-service ⏳ TODO

**Required Tests:**
- ⏳ GdprComplianceServiceTest
- ⏳ ComplianceControllerTest
- ⏳ SecurityConfigTest

### dms-llm-service ⏳ TODO

**Required Tests:**
- ⏳ SecureLlmQueryServiceTest
- ⏳ LlmQueryControllerTest
- ⏳ AzureConfigTest
- ⏳ SecurityConfigTest

## Running Tests

### Individual Service
```bash
cd dms-admin-service
mvn clean test
mvn jacoco:report
```

### All Services
```bash
./run-all-tests.sh  # To be created
```

### Coverage Report
After running tests, view coverage report:
```bash
open target/site/jacoco/index.html
```

## Coverage Goals

| Service | Current Coverage | Target Coverage | Status |
|---------|------------------|-----------------|--------|
| dms-admin-service | ~60% | 90% | In Progress |
| dms-document-service | 0% | 90% | Not Started |
| dms-audit-service | 0% | 90% | Not Started |
| dms-compliance-service | 0% | 90% | Not Started |
| dms-llm-service | 0% | 90% | Not Started |

## Test Best Practices

1. **Arrange-Act-Assert Pattern** - Clear test structure
2. **Test Isolation** - Each test is independent
3. **Mock External Dependencies** - Use Mockito for external services
4. **Test Edge Cases** - Null checks, empty collections, invalid input
5. **Test Error Scenarios** - Exception handling
6. **Meaningful Test Names** - `testMethodName_Scenario_ExpectedResult`
7. **Test Coverage** - Aim for 90%+ but focus on meaningful tests

## CI/CD Integration

Tests must pass in CI/CD pipeline:
- All tests must be stable and repeatable
- Coverage reports generated automatically
- Build fails if coverage < 90%
- Coverage reports published as artifacts
