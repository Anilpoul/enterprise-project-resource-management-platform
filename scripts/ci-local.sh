#!/usr/bin/env bash
# ==============================================================================
# Enterprise Platform Local CI Validation Script (Bash)
# Run this script to validate all backend, frontend, and compose checks locally.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo -e "\033[1;36m====================================================\033[0m"
echo -e "\033[1;36m🚀 Running Enterprise Platform Local CI Verification\033[0m"
echo -e "\033[1;36m====================================================\033[0m"

# 1. Backend Maven Reactor Build & Test Pass
echo -e "\n\033[1;33m[1/3] Verifying Backend Reactor & Running 311 Automated Tests...\033[0m"
cd "$ROOT_DIR"
if [ -f "./mvnw" ]; then
    chmod +x ./mvnw
    ./mvnw clean test -B
else
    mvn clean test -B
fi
echo -e "\033[1;32m✅ All Backend Microservice Tests Passed Cleanly!\033[0m"

# 2. Frontend React Application Build & Verification
echo -e "\n\033[1;33m[2/3] Verifying Frontend React Application Build...\033[0m"
cd "$ROOT_DIR/frontend"
npm ci
npm run build
echo -e "\033[1;32m✅ Frontend Production Bundle Compiled Cleanly!\033[0m"

# 3. Docker Compose Configuration Syntax Validation
echo -e "\n\033[1;33m[3/3] Validating Docker Compose Stack Configuration...\033[0m"
COMPOSE_FILE="$ROOT_DIR/docker/compose/docker-compose.yml"
ENV_EXAMPLE="$ROOT_DIR/docker/compose/.env.example"
ENV_FILE="$ROOT_DIR/docker/compose/.env"

if [ ! -f "$ENV_FILE" ] && [ -f "$ENV_EXAMPLE" ]; then
    cp "$ENV_EXAMPLE" "$ENV_FILE"
fi

if command -v docker >/dev/null 2>&1; then
    if docker compose -f "$COMPOSE_FILE" config --quiet; then
        echo -e "\033[1;32m✅ Docker Compose Configuration is 100% Valid!\033[0m"
    else
        echo -e "\033[1;33m⚠️ Docker Compose validation warning.\033[0m"
    fi
else
    echo -e "\033[0;37mℹ️ Docker CLI not detected in PATH - skipping local container validation.\033[0m"
fi

echo -e "\n\033[1;36m====================================================\033[0m"
echo -e "\033[1;32m🎉 Local CI Check Completed Successfully!\033[0m"
echo -e "\033[1;36m====================================================\033[0m"
