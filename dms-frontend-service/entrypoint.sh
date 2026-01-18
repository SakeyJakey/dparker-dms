#!/bin/sh
# Entrypoint script for nginx in Kubernetes

GATEWAY_RAW="${APIGATEWAY_SERVICE_URL:-${API_GATEWAY_URL:-dms-api-gateway-service:8080}}"
echo "INFO: GATEWAY_RAW from env: ${GATEWAY_RAW}"

API_GATEWAY_FULL=$(echo "$GATEWAY_RAW" | sed -E 's|^https?://||' | sed 's|/.*$||')
API_GATEWAY_HOST=$(echo "$API_GATEWAY_FULL" | cut -d: -f1)
API_GATEWAY_PORT=$(echo "$API_GATEWAY_FULL" | cut -d: -f2)
if [ -z "$API_GATEWAY_PORT" ]; then
    API_GATEWAY_PORT="8080"
fi
export API_GATEWAY_HOST
export API_GATEWAY_PORT
echo "INFO: Parsed API_GATEWAY_HOST=${API_GATEWAY_HOST} API_GATEWAY_PORT=${API_GATEWAY_PORT}"

NGINX_CONF="/tmp/nginx.conf"
envsubst '${API_GATEWAY_HOST} ${API_GATEWAY_PORT}' < /etc/nginx/nginx.conf > "$NGINX_CONF"

CACHE_DIR="/tmp/cache/nginx"
mkdir -p "$CACHE_DIR/client_temp" "$CACHE_DIR/proxy_temp" "$CACHE_DIR/fastcgi_temp" "$CACHE_DIR/uwsgi_temp" "$CACHE_DIR/scgi_temp"
chmod -R 777 "$CACHE_DIR" 2>/dev/null || true

PID_FILE="/tmp/nginx.pid"

echo "INFO: Starting nginx with config $NGINX_CONF"
exec nginx -c "$NGINX_CONF" -g "daemon off; pid $PID_FILE;"
