# Security Requirements - Zero CVE Policy

## Overview

This project maintains a **zero CVE policy** - all dependencies must be free from known Common Vulnerabilities and Exposures (CVEs).

## Technology Stack

### Backend
- **Java 25 LTS** - Required version
- **Spring Boot 3.4.x** - Framework
- **Maven** - Build tool

### Frontend
- **Angular 21** - Required version (NOT Angular 25)
- **TypeScript** - Language
- **Node.js 20+** - Runtime

### Infrastructure
- **Azure AKS** - Kubernetes platform
- **Flux** - GitOps configuration management
- **Istio** - Service mesh

## CVE Prevention Strategy

### 1. Automated Scanning

#### Maven Projects (Backend Services)
- **OWASP Dependency Check Maven Plugin** - Required in all POMs
- Runs automatically on every build
- Build fails if CVEs detected
- Reports generated in `target/dependency-check-report.html`

#### NPM Projects (Frontend)
- **npm audit** - Required before build
- Runs in CI/CD pipeline
- Build fails if vulnerabilities detected
- `npm audit fix` for automatic patching

#### Container Images
- **Trivy** - Container image scanning
- Scans all Docker images before deployment
- Integrated in CI/CD pipeline
- Blocks deployment if CVEs found

### 2. Dependency Management

#### Maven Dependencies
```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>10.0.4</version>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <failBuildOnCVSS>0</failBuildOnCVSS>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### NPM Dependencies
```json
{
  "scripts": {
    "audit": "npm audit --audit-level=moderate",
    "audit:fix": "npm audit fix"
  }
}
```

### 3. Regular Updates

- **Monthly Security Reviews** - All dependencies reviewed
- **Immediate Patching** - Critical CVEs patched within 24 hours
- **Version Pinning** - Specific versions to avoid unexpected updates
- **Security Advisories** - Monitor CVE databases

### 4. Approval Process

- **Dependency Approval** - New dependencies require security review
- **CVE Suppressions** - Only with documented justification
- **Security Team Review** - All suppressions must be approved

## CI/CD Integration

### Pre-Build Checks
1. OWASP Dependency Check (Maven)
2. npm audit (Frontend)
3. Container image scanning (Trivy)

### Build Failure Conditions
- Any CVE with CVSS score > 0
- Unapproved dependencies
- Outdated dependencies with known vulnerabilities

### Automated Workflows
- Weekly security scans
- Pull request security checks
- Pre-deployment validation

## Suppression Policy

### When Suppressions Are Allowed
1. **False Positives** - Verified false positive
2. **Accepted Risk** - Documented business justification
3. **Mitigation** - Alternative security controls in place

### Suppression Requirements
- Must be documented in `owasp-suppressions.xml`
- Requires security team approval
- Must include justification
- Regular review (quarterly)

## Monitoring

### Tools
- **OWASP Dependency Check** - Maven projects
- **npm audit** - Frontend projects
- **Trivy** - Container images
- **GitHub Dependabot** - Automated alerts
- **Snyk** - Additional scanning (optional)

### Reporting
- Security scan reports in CI/CD artifacts
- Weekly security summary
- CVE tracking dashboard
- Alert notifications for new CVEs

## Response Procedures

### Critical CVE (CVSS 9.0+)
- **Immediate Action** - Patch within 24 hours
- **Emergency Deployment** - Hotfix deployment
- **Communication** - Stakeholder notification

### High CVE (CVSS 7.0-8.9)
- **Priority Action** - Patch within 7 days
- **Scheduled Deployment** - Next release cycle
- **Monitoring** - Enhanced monitoring

### Medium CVE (CVSS 4.0-6.9)
- **Planned Action** - Patch within 30 days
- **Regular Deployment** - Next planned release
- **Assessment** - Risk assessment

### Low CVE (CVSS 0.1-3.9)
- **Scheduled Action** - Patch within 90 days
- **Bulk Updates** - Included in regular updates
- **Documentation** - Tracked in backlog

## Compliance

### Standards
- **OWASP Top 10** - Security best practices
- **CWE** - Common Weakness Enumeration
- **NIST** - Security guidelines
- **ISO 27001** - Information security management

### Audits
- Quarterly security audits
- Dependency review meetings
- CVE tracking and reporting
- Compliance documentation

## Resources

- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)
- [npm audit](https://docs.npmjs.com/cli/v10/commands/npm-audit)
- [Trivy](https://github.com/aquasecurity/trivy)
- [CVE Database](https://cve.mitre.org/)
- [NVD](https://nvd.nist.gov/)
