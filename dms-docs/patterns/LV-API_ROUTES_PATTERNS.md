# LV API Routes Patterns

This document describes the standard LV patterns for API routing in microservices applications. These patterns are application-generic and can be applied to any microservices architecture.

## Overview

This document covers:
- API Gateway routing patterns
- Frontend to API Gateway communication
- Service-to-service communication
- Path-based routing
- CORS configuration

## Traffic Flow Architecture

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
      ├──────────────────►  /         →  Frontend Service
      │
      └──────────────────►  /api/*    →  API Gateway
                                      │
                                      ▼
                          ┌───────────────────────────┐
                          │  API Gateway              │
                          │  Path-based routing:      │
                          │  /api/auth/**     → auth  │
                          │  /api/customers/**→ cust  │
                          │  /api/policies/** → pol   │
                          └───────────────────────────┘
                                      │
                                      ▼
                          ┌───────────────────────────┐
                          │  Backend Services         │
                          │  (mTLS encrypted)         │
                          └───────────────────────────┘
```

## Key Principles

1. **All traffic via API Gateway**: Frontend and external clients only talk to API Gateway
2. **No direct frontend → backend**: Frontend never calls backend services directly
3. **Path-based routing**: API Gateway routes based on URL path
4. **Service isolation**: Services communicate via API Gateway or service mesh
5. **CORS at Gateway**: CORS handled at API Gateway level

## Frontend to API Gateway

### Frontend Nginx Configuration

**Location**: `<app>-frontend-service/nginx.conf`

```nginx
# All /api/* requests proxied to API Gateway
location ~ ^/api/ {
    access_log /dev/stdout proxy_log;
    proxy_pass http://api_gateway;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header Connection "";
    proxy_read_timeout 300s;
    proxy_connect_timeout 75s;
}

# Upstream for API Gateway
upstream api_gateway {
    server ${API_GATEWAY_HOST}:${API_GATEWAY_PORT};
    keepalive 32;
}
```

**Key Points**:
- Single `/api/*` location block (no service-specific routes)
- Upstream block with environment variable substitution
- Keepalive connections for performance
- All `/api/*` requests go to API Gateway

### Frontend Angular Service

**Pattern**: Use relative URLs

```typescript
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = '';  // Empty = relative URLs
  
  constructor(private http: HttpClient) {}
  
  // All API calls use relative paths
  getCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(`${this.baseUrl}/api/customers`);
  }
  
  getPolicies(): Observable<Policy[]> {
    return this.http.get<Policy[]>(`${this.baseUrl}/api/policies`);
  }
}
```

**Why Relative URLs?**
- Works in all environments (dev, uat, prod)
- Nginx proxies `/api/*` to API Gateway automatically
- No environment-specific configuration needed
- Supports Docker Compose and Kubernetes

## API Gateway Routing

### Path-Based Routing

**Pattern**: Route based on URL path prefix

```
/api/auth/**          → auth-service
/api/customers/**     → customer-service
/api/policies/**      → policy-service
/api/claims/**        → claim-service
/api/payments/**      → payment-service
/api/documents/**     → document-service
/api/audit/**         → audit-service
```

### API Gateway Configuration

**Spring Cloud Gateway Example**:

```java
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .uri("http://auth-service:8080"))
            .route("customer-service", r -> r
                .path("/api/customers/**")
                .uri("http://customer-service:8080"))
            .route("policy-service", r -> r
                .path("/api/policies/**")
                .uri("http://policy-service:8080"))
            .route("document-service", r -> r
                .path("/api/documents/**")
                .uri("http://document-service:8080"))
            .route("audit-service", r -> r
                .path("/api/audit/**")
                .uri("http://audit-service:8080"))
            .build();
    }
}
```

**Service URLs from ConfigMap**:

```java
@Configuration
@ConfigurationProperties(prefix = "services")
public class ServiceUrlsConfig {
    
    private String authService;
    private String customerService;
    private String policyService;
    private String documentService;
    private String auditService;
    
    // Getters and setters
}
```

**application.yml**:

```yaml
services:
  auth-service: ${AUTH_SERVICE_URL:http://auth-service:8080}
  customer-service: ${CUSTOMER_SERVICE_URL:http://customer-service:8080}
  policy-service: ${POLICY_SERVICE_URL:http://policy-service:8080}
  document-service: ${DOCUMENT_SERVICE_URL:http://document-service:8080}
  audit-service: ${AUDIT_SERVICE_URL:http://audit-service:8080}
```

## Service URLs ConfigMap

### Backend ConfigMap

**Location**: `clusters/<env>/releases/<app>/<env>/config-maps/<app>-service-urls-configmap.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: <app>-service-urls-configmap
  namespace: <app>-<env>
data:
  apigateway.service: "http://<app>-api-gateway-service.<app>-<env>.svc.cluster.local:8080"
  auth.service: "http://<app>-auth-service.<app>-<env>.svc.cluster.local:8080"
  customer.service: "http://<app>-customer-service.<app>-<env>.svc.cluster.local:8080"
  policy.service: "http://<app>-policy-service.<app>-<env>.svc.cluster.local:8080"
  document.service: "http://<app>-document-service.<app>-<env>.svc.cluster.local:8080"
  audit.service: "http://<app>-audit-service.<app>-<env>.svc.cluster.local:8080"
```

**Naming Convention**:
- Service name pattern: `<app>-<service>-service`
- FQDN pattern: `<app>-<service>-service.<app>-<env>.svc.cluster.local:8080`
- Port: Always `8080` for backend services

### Frontend ConfigMap

**Location**: `clusters/<env>/releases/<app>/<env>/config-maps/<app>-frontend-configmap.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: <app>-service-urls-configmap
  namespace: <app>-<env>-frontend
data:
  apigateway.service: "http://<app>-api-gateway-service.<app>-<env>.svc.cluster.local:8080"
```

**Key Points**:
- Frontend only needs API Gateway URL
- All `/api/*` requests are proxied to API Gateway
- Frontend never talks directly to backend services

## Istio Virtual Services

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
    - "<app>.<env>.lvad.lvfs.net"
  gateways:
    - istio-ingress-internal/default-gateway-<env>-internal
  http:
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
```

## CORS Configuration

### API Gateway CORS

CORS is configured at the API Gateway level:

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "https://<app>.dev.lvad.lvfs.net",
            "https://<app>.uat.lvad.lvfs.net",
            "https://<app>.prod.lvad.lvfs.net"
        ));
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Accept", "Origin", "User-Agent"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = 
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### Istio CORS

CORS is also configured in Istio VirtualService:

```yaml
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

## Service-to-Service Communication

### Via API Gateway

**Pattern**: Services call other services via API Gateway

```java
@Service
public class CustomerService {
    
    @Value("${services.apigateway.service}")
    private String apiGatewayUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Policy getPolicyForCustomer(Long customerId) {
        // Call via API Gateway
        return restTemplate.getForObject(
            apiGatewayUrl + "/api/policies/customer/" + customerId,
            Policy.class
        );
    }
}
```

### Direct Service-to-Service (Service Mesh)

**Pattern**: Services call directly via service mesh (mTLS)

```java
@Service
public class CustomerService {
    
    @Value("${services.policy.service}")
    private String policyServiceUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Policy getPolicyForCustomer(Long customerId) {
        // Direct call via service mesh (mTLS encrypted)
        return restTemplate.getForObject(
            policyServiceUrl + "/api/policies/customer/" + customerId,
            Policy.class
        );
    }
}
```

**Key Points**:
- Use service URLs from ConfigMap
- Service mesh provides mTLS encryption
- Service discovery via Kubernetes DNS

## Path Conventions

### Standard API Paths

```
/api/auth/**              Authentication endpoints
/api/customers/**         Customer management
/api/policies/**          Policy management
/api/claims/**            Claim processing
/api/payments/**          Payment processing
/api/documents/**         Document management
/api/audit/**             Audit logging
/api/admin/**             Administration
/api/health               Health check
```

### Versioning

**Pattern**: Include version in path (optional)

```
/api/v1/auth/**
/api/v1/customers/**
/api/v2/policies/**
```

**Recommendation**: Use versioning for major API changes, but keep it simple.

## Best Practices

1. **Single Entry Point**: All external traffic via API Gateway
2. **Path-Based Routing**: Route based on URL path prefix
3. **Relative URLs**: Use relative URLs in frontend
4. **Service Discovery**: Use Kubernetes DNS for service discovery
5. **CORS Configuration**: Configure CORS at API Gateway level
6. **Service Isolation**: Services communicate via API Gateway or service mesh
7. **ConfigMap Usage**: Store service URLs in ConfigMaps
8. **Health Checks**: Include health endpoints for all services
9. **Error Handling**: Consistent error responses across services
10. **Documentation**: Document all API endpoints with OpenAPI

## Troubleshooting

### 502 Bad Gateway

**Error**: `502 Bad Gateway` when calling `/api/*`

**Solution**:
- Verify API Gateway service is running
- Check service URLs in ConfigMap
- Verify backend services are healthy
- Review API Gateway logs

### CORS Errors

**Error**: `CORS policy: No 'Access-Control-Allow-Origin' header`

**Solution**:
- Verify CORS configuration in API Gateway
- Check allowed origins match frontend URL
- Ensure `allowCredentials: true` for authenticated requests
- Review Istio VirtualService CORS policy

### Service Not Found

**Error**: `Service not found` or `Connection refused`

**Solution**:
- Verify service name includes `-service` suffix
- Check service exists in correct namespace
- Verify service selector matches pod labels
- Review service URLs in ConfigMap

## References

- [Service Mapping in K8s](../reference/service-mapping-in-k8s.md)
- [Angular + Nginx Frontend Setup Pattern](./LV-FRONTEND_PATTERNS.md)
- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-mendix-test/docs/k8s/service-mapping-in-k8s.md`
  - `/Users/davidparker/Documents/LV-Code/dparker-rti-now/clusters/dev/releases/rti/dev/istio-configs/virtual-services/`

## Version History

- **2026-01-XX**: Initial pattern documentation based on LV production implementations
