#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"

cd "${PROJECT_DIR}"

echo "Restarting HR system..."

"${SCRIPT_DIR}/stop.sh"

echo "Waiting for port to be released..."
sleep 2

echo "Starting HR system..."
mvn spring-boot:run
