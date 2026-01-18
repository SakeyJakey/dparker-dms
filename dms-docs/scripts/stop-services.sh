#!/bin/bash

# Stop all DMS services

set -e

echo "🛑 Stopping DMS services..."

docker-compose down

echo "✅ All services stopped."
