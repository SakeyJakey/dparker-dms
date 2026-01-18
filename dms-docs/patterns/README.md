# LV Production Patterns

This directory contains application-generic patterns for implementing microservices applications following LV (Legal & General) production standards.

## Pattern Documents

1. **[LV-FLUX_PATTERNS.md](./LV-FLUX_PATTERNS.md)** - Flux GitOps configuration patterns
2. **[LV-ISTIO_PATTERNS.md](./LV-ISTIO_PATTERNS.md)** - Istio service mesh patterns
3. **[LV-K8S_DEPLOYMENT_PATTERNS.md](./LV-K8S_DEPLOYMENT_PATTERNS.md)** - Kubernetes deployment patterns
4. **[LV-FRONTEND_PATTERNS.md](./LV-FRONTEND_PATTERNS.md)** - Angular frontend with Nginx patterns
5. **[LV-AUTHENTICATION_PATTERNS.md](./LV-AUTHENTICATION_PATTERNS.md)** - Azure AD authentication patterns
6. **[LV-API_ROUTES_PATTERNS.md](./LV-API_ROUTES_PATTERNS.md)** - API routing and gateway patterns

## Purpose

These patterns are designed to be:
- **Application-generic**: Can be applied to any microservices application
- **Production-ready**: Based on proven implementations in LV production environments
- **Comprehensive**: Cover all aspects from deployment to authentication
- **Maintainable**: Clear structure and naming conventions

## Usage

When implementing a new microservices application:

1. Review all pattern documents
2. Follow naming conventions and directory structures
3. Use ConfigMap patterns for service URLs
4. Implement authentication following Azure AD patterns
5. Configure frontend to proxy all `/api/*` to API Gateway
6. Set up Flux for GitOps deployment
7. Configure Istio for service mesh capabilities

## Reference Implementations

These patterns are based on working implementations in:
- `/Users/davidparker/Documents/LV-Code/dparker-rti-now`
- `/Users/davidparker/Documents/LV-Code/dparker-mendix-test`
- `/Users/davidparker/Documents/LV-Code/dparker-demos`

## Key Principles

1. **All traffic via API Gateway**: Frontend and external clients only talk to API Gateway
2. **Port standardization**: All services use port 8080
3. **Service naming**: Pattern `<app>-<service>-service`
4. **Namespace separation**: Frontend and backend in separate namespaces
5. **ConfigMap usage**: Service URLs stored in ConfigMaps
6. **mTLS everywhere**: STRICT mode for all service-to-service communication
7. **GitOps**: All configuration managed via Flux
8. **No OWASP in K8s**: OWASP scanning only in CI/CD, not in Kubernetes deployments

## Version History

- **2026-01-XX**: Initial pattern documentation based on LV production implementations
