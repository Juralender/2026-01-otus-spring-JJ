#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$SCRIPT_DIR/frontend"
STATIC_DIR="$SCRIPT_DIR/src/main/resources/static"
NODE_IMAGE="node:22.21.1-alpine"

echo "Building frontend in $NODE_IMAGE..."
docker run --rm -v "$FRONTEND_DIR":/app -w /app "$NODE_IMAGE" \
  sh -c "npm install --no-audit --no-fund && npm run build"

echo "Copying build output to $STATIC_DIR"
rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -r "$FRONTEND_DIR"/dist/* "$STATIC_DIR"/

echo "Done. Frontend build copied to src/main/resources/static"
