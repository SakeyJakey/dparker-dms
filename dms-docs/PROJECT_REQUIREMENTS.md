---
Last Updated: 2024-01-18T13:32:00Z
Updated By: davidparker-lv-bmth
---

# DMS Project Requirements

## Technology Stack

### Backend
- **Java 25 LTS** - Required version (strict)
- **Spring Boot 3.4.x** - Framework version
- **Maven** - Build tool
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework

### Frontend
- **Angular 21** - Required version (strict, NOT Angular 25)
- **TypeScript** - Language
- **Angular Material 21** - UI component library
- **MSAL Angular** - Azure AD authentication
- **Node.js 20+** - Runtime

### Infrastructure
- **Azure AKS (Azure Kubernetes Service)** - Container orchestration platform
- **Flux** - GitOps configuration management for Kubernetes
- **Istio** - Service mesh for microservices communication
- **Docker** - Containerization
- **Kubernetes 1.28+** - Container orchestration

## Code Quality Requirements

### Unit Test Coverage
- **Minimum 90% code coverage** required for all services
- JaCoCo Maven plugin configured with 90% threshold
- Build fails if coverage < 90%
- All services must have comprehensive test suites

### Frontend Standards
- **WCAG 2.1 Level AA compliance** mandatory
- Semantic HTML with ARIA labels
- Keyboard navigation support
- Screen reader compatibility
- High contrast (4.5:1 ratio minimum)
- Responsive design

## Security Requirements

### Zero CVE Policy
- **No CVE vulnerabilities** allowed in dependencies
- **OWASP Dependency Check** - Required in all Maven projects
- **npm audit** - Required for frontend (fails on moderate+)
- **Container scanning** - Trivy scans all Docker images
- **Automated scanning** - CI/CD pipeline enforces checks

### Security Tools
- **OWASP Dependency Check Maven Plugin** - Version 10.0.4
- **npm audit** - Built into npm
- **Trivy** - Container image scanning
- **GitHub Actions** - Automated security workflows

## Infrastructure Requirements

### Azure AKS
- **Kubernetes Version** - 1.28 or higher
- **Node Pools** - Auto-scaling enabled
- **Network Plugin** - Azure CNI
- **RBAC** - Enabled
- **Pod Security Standards** - Restricted

### Flux GitOps
- **Git Repository** - https://github.com/davidparker-lv-bmth/dparker-dms
- **Sync Interval** - 5 minutes
- **Configuration Path** - ./k8s
- **Health Checks** - Automatic validation

### Istio Service Mesh
- **mTLS Mode** - STRICT (required)
- **Traffic Management** - VirtualServices and DestinationRules
- **Security Policies** - AuthorizationPolicy and PeerAuthentication
- **Observability** - Metrics, logs, traces
- **Circuit Breakers** - Fault tolerance

## Build Requirements

### Maven Builds
- OWASP Dependency Check must pass (failBuildOnCVSS=0)
- JaCoCo coverage must be ≥ 90%
- All tests must pass
- No CVEs detected

### Frontend Builds
- npm audit must pass (no moderate+ vulnerabilities)
- Angular 21 version enforced
- TypeScript strict mode enabled
- Linting must pass

### CI/CD Pipeline
- Automated security scanning
- Test execution and coverage verification
- Container image scanning
- Flux sync validation
- Istio policy validation

## Application Identifier

- All identifiers use `davidparker-lv-bmth`
- Storage containers: `davidparker-lv-bmth-documents`
- Application role: `DMS.davidparker-lv-bmth`
- Key Vault secrets: `davidparker-lv-bmth-*` prefix

## Compliance

- **PCI-DSS** - Payment card data protection
- **GDPR** - Data subject rights
- **ISO 27001** - Information security management
- **WCAG 2.1 AA** - Web accessibility

## Documentation

- All APIs documented with OpenAPI 3.0
- Architecture documentation maintained
- Security requirements documented
- Deployment procedures documented
- Runbooks for operations
