# LV Frontend Service Patterns

This document describes the standard LV patterns for Angular frontend services with Nginx in Kubernetes environments. These patterns are application-generic and can be applied to any Angular frontend application.

## Overview

This pattern ensures Angular frontend services work reliably in Kubernetes by:
- Using port 8080 instead of 80 to avoid permission issues
- Using an entrypoint script to handle runtime configuration
- Properly managing nginx cache directories and PID files
- Supporting both Docker Compose and Kubernetes deployments
- Proxying all `/api/*` requests to API Gateway

## Directory Structure

```
<app>-frontend-service/
├── Dockerfile
├── nginx.conf
├── entrypoint.sh
├── angular.json
├── package.json
└── src/
    └── app/
```

## Nginx Configuration

### Standard nginx.conf

**Location**: `<app>-frontend-service/nginx.conf`

```nginx
# Nginx configuration for frontend-service
# Note: PID file location is set via command-line flag in entrypoint.sh
# Using /tmp for PID file as it's always writable in Kubernetes

# Worker processes
worker_processes auto;
# Log to stderr for Kubernetes log aggregation
error_log /dev/stderr info;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Log formats for Kubernetes
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    log_format proxy_log '$remote_addr - $remote_user [$time_local] "$request" '
                        '$status $body_bytes_sent "$http_referer" '
                        '"$http_user_agent" "$http_x_forwarded_for" '
                        'upstream: $upstream_addr '
                        'upstream_status: $upstream_status '
                        'upstream_response_time: $upstream_response_time '
                        'request_time: $request_time';

    # Log to stdout for Kubernetes log aggregation
    access_log /dev/stdout main;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/json application/javascript;

    # Use Docker's internal DNS resolver for dynamic hostname resolution
    resolver 127.0.0.11 valid=30s;

    # Upstream for API Gateway (substituted at runtime)
    upstream api_gateway {
        server ${API_GATEWAY_HOST}:${API_GATEWAY_PORT};
        keepalive 32;
    }

    server {
        # Listen on port 8080 (Kubernetes standard, avoids permission issues)
        listen 8080;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;

        # API proxy routes (specific routes first, then catch-all)
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

        # Static assets
        location ~ ^/(runtime|polyfills|main|styles|favicon|.*\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)) {
            expires 1y;
            add_header Cache-Control "public, immutable";
            try_files $uri =404;
        }

        # Health check endpoint
        location /health {
            access_log off;
            default_type application/json;
            return 200 '{"status":"UP","components":{"nginx":{"status":"UP"},"frontend":{"status":"UP"}}}';
        }

        # Angular routing - serve index.html for all routes (must be last)
        location = /index.html {
            add_header Cache-Control "no-cache, no-store, must-revalidate";
            add_header Pragma "no-cache";
            add_header Expires "0";
            try_files $uri =404;
        }
        
        location / {
            try_files $uri $uri/ /index.html;
        }
    }
}
```

**Key Points**:
- **DO NOT** use `user root;` directive
- Use port **8080** instead of 80
- Log to `/dev/stdout` and `/dev/stderr` for Kubernetes
- Use `/tmp` for PID file (set via entrypoint)
- Upstream block with `${API_GATEWAY_HOST}` and `${API_GATEWAY_PORT}` placeholders
- All `/api/*` proxied to API Gateway
- Angular routing: `try_files $uri $uri/ /index.html;`

## Entrypoint Script

### Standard entrypoint.sh

**Location**: `<app>-frontend-service/entrypoint.sh`

```bash
#!/bin/sh
# Entrypoint script for nginx in Kubernetes
# Simplified approach: use /tmp for PID file (always writable in K8s)

# Create cache directories (required for nginx operation)
mkdir -p /var/cache/nginx/client_temp
mkdir -p /var/cache/nginx/proxy_temp
mkdir -p /var/cache/nginx/fastcgi_temp
mkdir -p /var/cache/nginx/uwsgi_temp
mkdir -p /var/cache/nginx/scgi_temp

# Set permissions for cache directories
chmod -R 755 /var/cache/nginx 2>/dev/null || true

# Parse API Gateway URL from environment variable
# Supports: APIGATEWAY_SERVICE_URL or API_GATEWAY_URL
API_GATEWAY_URL="${APIGATEWAY_SERVICE_URL:-${API_GATEWAY_URL:-api-gateway:7080}}"

# Extract host and port from URL
# Handles: http://host:port, https://host:port, host:port
API_GATEWAY_URL=$(echo "$API_GATEWAY_URL" | sed 's|^https\?://||')
API_GATEWAY_HOST=$(echo "$API_GATEWAY_URL" | cut -d: -f1)
API_GATEWAY_PORT=$(echo "$API_GATEWAY_URL" | cut -d: -f2)

# Default port if not specified
if [ -z "$API_GATEWAY_PORT" ] || [ "$API_GATEWAY_PORT" = "$API_GATEWAY_HOST" ]; then
    API_GATEWAY_PORT=8080
fi

# Use /tmp for PID file - always writable in Kubernetes containers
PID_FILE="/tmp/nginx.pid"

# Substitute environment variables in nginx.conf
echo "INFO: Substituting API Gateway URL: ${API_GATEWAY_HOST}:${API_GATEWAY_PORT}"
envsubst '${API_GATEWAY_HOST} ${API_GATEWAY_PORT}' < /etc/nginx/nginx.conf > /tmp/nginx.conf

# Test nginx config syntax
echo "INFO: Testing nginx configuration syntax..."
NGINX_TEST_OUTPUT=$(nginx -t -c /tmp/nginx.conf -g "pid /tmp/nginx-test.pid;" 2>&1)
NGINX_TEST_EXIT=$?

if [ $NGINX_TEST_EXIT -ne 0 ]; then
    echo "ERROR: Nginx configuration test failed:"
    echo "$NGINX_TEST_OUTPUT"
    # Check if it's a DNS resolution error (acceptable during startup)
    if echo "$NGINX_TEST_OUTPUT" | grep -q "host not found in upstream"; then
        echo "WARNING: DNS resolution failed, but this is acceptable during startup"
        echo "INFO: Proceeding with nginx start (DNS will resolve at runtime)"
    else
        echo "ERROR: Configuration error is not DNS-related, exiting"
        exit 1
    fi
else
    echo "INFO: Nginx configuration test passed"
fi

# Start nginx with PID file in /tmp
echo "INFO: Starting nginx..."
exec nginx -c /tmp/nginx.conf -g "daemon off; pid $PID_FILE;"
```

**Key Features**:
- Creates nginx cache directories at runtime
- Parses API Gateway URL from environment variable
- Substitutes variables in nginx.conf using `envsubst`
- Writes rendered config to `/tmp/nginx.conf` (not `/etc/nginx/`)
- Handles DNS resolution failures gracefully
- Uses `/tmp` for PID file

## Dockerfile

### Production Dockerfile

**Location**: `<app>-frontend-service/Dockerfile`

```dockerfile
# Stage 1: Build the Angular application
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY ./ /app

# Build the application
RUN npm run build:prod

# Stage 2: Serve with nginx
FROM nginx:alpine

# Install gettext for envsubst (environment variable substitution)
RUN apk add --no-cache gettext

# Copy built application from build stage
COPY --from=build /app/dist/<app>-frontend-service /usr/share/nginx/html

# Copy nginx configuration (complete file, replaces default nginx.conf)
COPY nginx.conf /etc/nginx/nginx.conf

# Copy entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose port 8080 (standard for Kubernetes, avoids permission issues)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/health || exit 1

CMD ["/entrypoint.sh"]
```

**Key Points**:
- Multi-stage build (build + runtime)
- Node 20 Alpine for build
- Nginx Alpine for runtime
- Install `gettext` for `envsubst`
- Copy built app to `/usr/share/nginx/html`
- Expose port 8080
- Health check on `/health` endpoint

## Angular Configuration

### API Base URL

**Critical**: The frontend must use **relative** API URLs so requests go to the same origin as the app; Nginx then proxies `/api/*` to the API gateway.

**ApiService Pattern**:

```typescript
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = '';  // Empty = relative URLs
  
  constructor(private http: HttpClient) {}
  
  get<T>(path: string): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}${path}`);
  }
  
  post<T>(path: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${path}`, body);
  }
}
```

**Why Relative URLs?**
- Works in all environments (dev, uat, prod)
- Nginx proxies `/api/*` to API Gateway
- No hardcoded URLs needed
- Supports Docker Compose and Kubernetes

### Local Development (ng serve)

For local development with `ng serve`, use `proxy.conf.json`:

```json
{
  "/api": {
    "target": "http://localhost:7080",
    "secure": false,
    "changeOrigin": true,
    "logLevel": "debug"
  }
}
```

**angular.json**:
```json
{
  "projects": {
    "<app>-frontend-service": {
      "architect": {
        "serve": {
          "options": {
            "proxyConfig": "proxy.conf.json"
          }
        }
      }
    }
  }
}
```

## Kubernetes Deployment

### Frontend Deployment

**Location**: `clusters/<env>/releases/<app>/<env>/frontend/<app>-frontend-service.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: <app>-frontend-service
  namespace: <app>-<env>-frontend
  labels:
    app: <app>-frontend-service
    service: frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: <app>-frontend-service
  template:
    metadata:
      labels:
        app: <app>-frontend-service
        service: frontend
    spec:
      containers:
      - name: <app>-frontend-service
        image: <app>-frontend-service:<tag>
        imagePullPolicy: Always
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        env:
        - name: APIGATEWAY_SERVICE_URL
          valueFrom:
            configMapKeyRef:
              name: <app>-service-urls-configmap
              key: apigateway.service
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
---
apiVersion: v1
kind: Service
metadata:
  name: <app>-frontend-service
  namespace: <app>-<env>-frontend
  labels:
    app: <app>-frontend-service
spec:
  type: ClusterIP
  ports:
  - name: http
    port: 8080
    targetPort: 8080
    protocol: TCP
  selector:
    app: <app>-frontend-service
```

**Key Points**:
- Namespace: `<app>-<env>-frontend`
- Port: 8080
- Environment variable: `APIGATEWAY_SERVICE_URL` from ConfigMap
- Health check: `/health` endpoint

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

**Critical**: The hostname must be `<app>-api-gateway-service` (with `-service`), **not** `<app>-api-gateway`. The K8s Service name is `<app>-api-gateway-service`.

## Common Issues and Solutions

### Issue 1: Permission Denied on Port 80

**Error**: `bind() to 0.0.0.0:80 failed (13: Permission denied)`

**Solution**:
- Use port **8080** instead of 80
- Remove `user root;` directive from nginx.conf
- Let nginx handle permissions automatically

### Issue 2: PID File Permission Denied

**Error**: `Permission denied` when writing PID file

**Solution**:
- Use `/tmp/nginx.pid` instead of default location
- Set via entrypoint script: `-g "pid /tmp/nginx.pid;"`
- `/tmp` is always writable in Kubernetes containers

### Issue 3: Cache Directory Permission Denied

**Error**: `mkdir() "/var/cache/nginx/client_temp" failed (13: Permission denied)`

**Solution**:
- Create cache directories in entrypoint script at runtime
- Use `chmod -R 755` on cache directories
- Don't set ownership in Dockerfile (let runtime handle it)

### Issue 4: DNS Resolution Failures

**Error**: `host not found in upstream` during config test

**Solution**:
- Entrypoint script should handle DNS failures gracefully
- DNS will resolve at runtime when containers are ready
- Don't fail config test on DNS errors

### Issue 5: API Gateway Not Found

**Error**: `502 Bad Gateway` when calling `/api/*`

**Solution**:
- Verify `APIGATEWAY_SERVICE_URL` environment variable is set
- Check ConfigMap exists and has correct key
- Verify API Gateway service name includes `-service` suffix
- Check API Gateway service is running and healthy

## Best Practices

1. **Port Selection**: Use **8080** for production (Kubernetes standard)
2. **Logging**: Always log to `/dev/stdout` and `/dev/stderr`
3. **Security**: Always include security headers
4. **Performance**: Enable gzip compression
5. **Caching**: Set appropriate cache headers for static assets
6. **Health Checks**: Always include health endpoint
7. **API Proxy**: All `/api/*` requests via API Gateway
8. **Relative URLs**: Use relative API URLs in Angular
9. **Environment Variables**: Use ConfigMaps for API Gateway URL
10. **DNS Handling**: Handle DNS resolution failures gracefully

## References

- [Angular Documentation](https://angular.io/docs)
- [Nginx Documentation](https://nginx.org/en/docs/)
- Reference implementations:
  - `/Users/davidparker/Documents/LV-Code/dparker-demos/clanad-microservices/frontend-service`
  - `/Users/davidparker/Documents/LV-Code/dparker-mendix-test/docs/frontend/angular-nginx-frontend-setup-pattern.md`

## Version History

- **2026-01-XX**: Initial pattern documentation based on LV production implementations
