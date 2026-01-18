# Stage 1: Build the Angular application
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files first
COPY package*.json ./

# Install dependencies (use --legacy-peer-deps to handle peer dependency conflicts)
RUN npm install --legacy-peer-deps

# Copy source code
COPY ./ /app

# Build the application (Angular CLI is installed via npm install)
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:alpine

# Install gettext for envsubst (environment variable substitution)
RUN apk add --no-cache gettext

# Copy built application from build stage
COPY --from=build /app/dist/dms-frontend/browser /usr/share/nginx/html

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
