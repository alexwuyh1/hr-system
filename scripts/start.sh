#!/usr/bin/env bash
set -euo pipefail

# Start the HR system in the background and write a PID file.
# Default port is 18080; override with PORT env var if needed.

PORT="${PORT:-18080}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_FILE="${ROOT_DIR}/logs/hr-system.log"
PID_FILE="${ROOT_DIR}/logs/hr-system.pid"

mkdir -p "${ROOT_DIR}/logs"

if [[ -f "${PID_FILE}" ]] && ps -p "$(cat "${PID_FILE}")" >/dev/null 2>&1; then
  echo "HR system already running with PID $(cat "${PID_FILE}")."
  exit 0
fi

echo "Starting HR system on port ${PORT}..."

(
  cd "${ROOT_DIR}"
  # Ensure JAVA_HOME for a clean environment.
  export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
  export PATH="${JAVA_HOME}/bin:${PATH}"
  mvn -q -DskipTests spring-boot:run -Dspring-boot.run.arguments="--server.port=${PORT}" \
    > "${LOG_FILE}" 2>&1 &
  echo $! > "${PID_FILE}"
)

sleep 2
if ps -p "$(cat "${PID_FILE}")" >/dev/null 2>&1; then
  echo "Started with PID $(cat "${PID_FILE}"). Logs: ${LOG_FILE}"
else
  echo "Start failed. Check logs: ${LOG_FILE}"
  exit 1
fi
