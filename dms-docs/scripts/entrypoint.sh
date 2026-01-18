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
