# LV Istio Service Mesh Patterns

This document describes the standard LV patterns for Istio service mesh configuration in Kubernetes environments. These patterns are application-generic and can be applied to any microservices application.

## Overview

Istio provides service mesh capabilities including:
- **mTLS**: Mutual TLS encryption between services
- **Traffic Management**: Routing, retries, timeouts, circuit breakers
- **Security Policies**: Authorization and authentication policies
- **Observability**: Metrics, logs, and distributed tracing

## Directory Structure

### Standard Structure

```
clusters/
└── <env>/
    ├── manifests/
    │   └── istio-configs/
    │       └── <env>/
    │           ├── authorization-policy.yaml
    │           └── request-authentication.yaml
    └── releases/
        └── <app>/
            └── <env>/
                └── istio-configs/
                    ├── authz-policies/
                    │   ├── <app>-api-gateway.yaml
                    │   └── <app>-auth-service.yaml
                    ├── destination-rules/
                    │   ├── <app>-auth-service.yaml
                    │   └── <app>-frontend-service.yaml
                    └── virtual-services/
                        ├── <app>-api-gateway-service.yaml
                        └── <app>-frontend-service.yaml
```

## Peer Authentication (mTLS)

### Global mTLS Policy

**Location**: `istio-config/peer-authentication.yaml` (root level) or environment-specific

```yaml
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: <app>-<env>
spec:
  mtls:
    mode: STRICT
```

**Key Points**:
- Mode: `STRICT` enforces mTLS for all service-to-service communication
- Namespace: Apply to each application namespace
- Security: All traffic encrypted between services

## Request Authentication

### Azure AD JWT Authentication

**Location**: `clusters/<env>/manifests/istio-configs/<env>/request-authentication.yaml`

```yaml
apiVersion: security.istio.io/v1
kind: RequestAuthentication
metadata:
  name: default-<env>-internal
  namespace: istio-system
spec:
  selector:
    matchLabels:
      istio: ingressgateway-<env>-internal
  jwtRules:
    # Azure AD issuer (RSA-signed, validated via JWKS)
    - issuer: "https://login.microsoftonline.com/${AZURE_TENANT_ID}/v2.0"
      jwksUri: "https://login.microsoftonline.com/${AZURE_TENANT_ID}/discovery/v2.0/keys"
      forwardOriginalToken: true
    
    # Application auth service issuer (HMAC-signed, cannot be validated by Istio)
    # Services validate these tokens using shared JWT_SECRET
    - issuer: "<app>-auth-service"
      forwardOriginalToken: true
```

**Key Points**:
- Azure AD tokens validated via JWKS endpoint
- Application tokens forwarded to services for validation
- `forwardOriginalToken: true` allows services to validate tokens

## Authorization Policies

### Ingress Gateway Authorization

**Location**: `clusters/<env>/manifests/istio-configs/<env>/authorization-policy.yaml`

```yaml
apiVersion: security.istio.io/v1
kind: AuthorizationPolicy
metadata:
  name: allow-request-for-<env>-internal-api
  namespace: istio-system
spec:
  selector:
    matchLabels:
      istio: ingressgateway-<env>-internal
  action: ALLOW
  rules:
    - to:
        - operation:
            hosts:
              - <app>.<env>.lvad.lvfs.net
            paths:
              - /api/*
```

**Key Points**:
- Applied to Istio ingress gateway
- Allows API traffic to specific host and paths
- Can be restricted further per service

### Service-Level Authorization

**Location**: `clusters/<env>/releases/<app>/<env>/istio-configs/authz-policies/<app>-api-gateway.yaml`

```yaml
apiVersion: security.istio.io/v1
kind: AuthorizationPolicy
metadata:
  name: <app>-api-gateway-policy
  namespace: <app>-<env>
spec:
  selector:
    matchLabels:
      app: <app>-api-gateway-service
  action: ALLOW
  rules:
    - from:
        - source:
            principals: ["cluster.local/ns/<app>-<env>/sa/<app>-frontend-service"]
      to:
        - operation:
            methods: ["GET", "POST", "PUT", "DELETE", "PATCH"]
            paths: ["/api/*"]
```

**Key Points**:
- Service-specific authorization
- Can restrict by source service account
- Method and path restrictions

## Virtual Services

### API Gateway Virtual Service

**Location**: `clusters/<env>/releases/<app>/<env>/istio-configs/virtual-services/<app>-api-gateway-service.yaml`

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: <app>-api-gateway-service-internal
  namespace: <app>-<env>
spec:
  hosts:
    - "<app>.<env>.lvad.lvfs.net"  # Same host as frontend
  gateways:
    - istio-ingress-internal/default-gateway-<env>-internal
  http:
    # Route API calls from frontend to API Gateway
    - match:
        - uri:
            prefix: "/api"
      route:
        - destination:
            host: <app>-api-gateway-service.<app>-<env>.svc.cluster.local
            port:
              number: 8080
      timeout: 120s
      retries:
        attempts: 3
        perTryTimeout: 10s
      corsPolicy:
        allowOrigins:
          - exact: https://<app>.<env>.lvad.lvfs.net
        allowMethods:
          - POST
          - GET
          - OPTIONS
          - PATCH
          - PUT
          - DELETE
        allowHeaders:
          - Authorization
          - Content-Type
          - Accept
          - Origin
          - User-Agent
        allowCredentials: true
```

**Key Points**:
- Routes `/api/*` to API Gateway
- CORS policy for frontend origin
- Timeout and retry configuration
- Gateway reference to Istio ingress

### Frontend Virtual Service

**Location**: `clusters/<env>/releases/<app>/<env>/istio-configs/virtual-services/<app>-frontend-service.yaml`

```yaml
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: <app>-frontend-service-internal
  namespace: <app>-<env>-frontend
spec:
  hosts:
    - "<app>.<env>.lvad.lvfs.net"
  gateways:
    - istio-ingress-internal/default-gateway-<env>-internal
  http:
    - match:
        - uri:
            prefix: "/"
      route:
        - destination:
            host: <app>-frontend-service.<app>-<env>-frontend.svc.cluster.local
            port:
              number: 8080
      timeout: 120s
      corsPolicy:
        allowOrigins:
          - exact: https://<app>.<env>.lvad.lvfs.net
        allowMethods:
          - POST
          - GET
          - OPTIONS
          - PATCH
          - PUT
          - DELETE
        allowHeaders:
          - Authorization
          - Content-Type
          - Accept
          - Origin
          - User-Agent
        allowCredentials: true
```

**Key Points**:
- Routes root path to frontend service
- Frontend namespace for service
- CORS policy matches API Gateway

## Destination Rules

### Service Destination Rules

**Location**: `clusters/<env>/releases/<app>/<env>/istio-configs/destination-rules/<app>-<service>-service.yaml`

```yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: <app>-<service>-service
  namespace: <app>-<env>
spec:
  host: <app>-<service>-service.<app>-<env>.svc.cluster.local
  trafficPolicy:
    tls:
      mode: ISTIO_MUTUAL
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 10
        http2MaxRequests: 100
        maxRequestsPerConnection: 2
        maxRetries: 3
        idleTimeout: 90s
    outlierDetection:
      consecutiveErrors: 3
      interval: 30s
      baseEjectionTime: 30s
      maxEjectionPercent: 50
      minHealthPercent: 50
  subsets:
    - name: v1
      labels:
        version: v1
```

**Key Points**:
- `ISTIO_MUTUAL` enforces mTLS
- Connection pool limits for resource management
- Circuit breaker via outlier detection
- Version subsets for canary deployments

### Global Destination Rule

**Location**: `istio-config/destination-rule.yaml` (root level)

```yaml
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: <app>-services
  namespace: <app>-<env>
spec:
  host: "*.<app>-<env>.svc.cluster.local"
  trafficPolicy:
    tls:
      mode: ISTIO_MUTUAL
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 10
        http2MaxRequests: 100
        maxRequestsPerConnection: 2
        maxRetries: 3
        idleTimeout: 90s
    outlierDetection:
      consecutiveErrors: 3
      interval: 30s
      baseEjectionTime: 30s
      maxEjectionPercent: 50
      minHealthPercent: 50
  subsets:
    - name: v1
      labels:
        version: v1
```

**Key Points**:
- Wildcard host matches all services in namespace
- Applies default traffic policy to all services
- Can be overridden by service-specific rules

## Traffic Flow Patterns

### Standard Flow

```
User / Browser
      │
      ▼
┌─────────────────────────────────────────────────────────┐
│  Istio Ingress Gateway                                   │
│  (https://<app>.<env>.lvad.lvfs.net)                     │
└─────────────────────────────────────────────────────────┘
      │
      ├──────────────────►  /         →  Frontend
      │
      └──────────────────►  /api/*    →  API Gateway
                                      │
                                      ▼
                          ┌───────────────────────────┐
                          │  API Gateway              │
                          │  Routes to backend        │
                          └───────────────────────────┘
                                      │
                                      ▼
                          ┌───────────────────────────┐
                          │  Backend Services         │
                          │  (mTLS encrypted)         │
                          └───────────────────────────┘
```

### Key Principles

1. **All traffic via Istio Gateway**: External traffic enters through Istio ingress
2. **Frontend → API Gateway**: Frontend proxies `/api/*` to API Gateway
3. **API Gateway → Backend**: API Gateway routes to backend services
4. **mTLS between services**: All service-to-service traffic encrypted
5. **CORS at Gateway**: CORS handled at ingress gateway level

## Security Patterns

### mTLS Configuration

- **Mode**: `STRICT` for all namespaces
- **Enforcement**: Via PeerAuthentication policy
- **Benefits**: Encrypted service-to-service communication

### JWT Validation

- **Azure AD**: Validated via JWKS endpoint
- **Application Tokens**: Forwarded to services for validation
- **Policy**: RequestAuthentication at ingress gateway

### Authorization

- **Gateway Level**: Allow/deny at ingress
- **Service Level**: Fine-grained per-service policies
- **Service Accounts**: Use service accounts for service identity

## Best Practices

1. **mTLS Everywhere**: Use STRICT mode for all services
2. **Gateway Policies**: Apply security policies at ingress gateway
3. **Service Isolation**: Separate namespaces for frontend and backend
4. **CORS Configuration**: Configure CORS at gateway level
5. **Circuit Breakers**: Use destination rules for fault tolerance
6. **Retry Policies**: Configure retries for transient failures
7. **Timeouts**: Set appropriate timeouts for all routes
8. **Version Subsets**: Use destination rule subsets for canary deployments
9. **Health Checks**: Ensure services have proper health endpoints
10. **Observability**: Enable metrics, logs, and tracing

## Troubleshooting

### mTLS Issues
- Check PeerAuthentication policy: `kubectl get peerauthentication -n <namespace>`
- Verify sidecar injection: `kubectl get pods -n <namespace> -o jsonpath='{.items[*].spec.containers[*].name}'`
- Check service account: Services need service accounts for mTLS

### Routing Issues
- Verify VirtualService: `kubectl get virtualservice -n <namespace>`
- Check gateway configuration: `kubectl get gateway -n istio-system`
- Review destination rules: `kubectl get destinationrule -n <namespace>`

### CORS Issues
- Verify CORS policy in VirtualService
- Check allowed origins match frontend URL
- Ensure `allowCredentials: true` for authenticated requests

## References

- [Istio Documentation](https://istio.io/latest/docs/)
- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/clusters/dev/manifests/istio-configs/`
  - `/Users/davidparker/Documents/LV-Code/dparker-dms/istio-config/`

## Version History

- **2026-01-XX**: Initial pattern documentation based on LV production implementations
