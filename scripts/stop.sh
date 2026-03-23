#!/usr/bin/env bash
set -euo pipefail

# Stop the HR system process by port or by known keywords.
# Default port is 18080; override with PORT env var if needed.

PORT="${PORT:-18080}"

echo "Stopping HR system on port ${PORT}..."

# Try to find PID by listening port first.
PID_BY_PORT=""
if command -v ss >/dev/null 2>&1; then
  PID_BY_PORT=$(ss -ltnp 2>/dev/null | awk -v p=":${PORT}" '$4 ~ p {print $NF}' | sed -n 's/.*pid=\\([0-9]*\\).*/\\1/p' | head -n 1)
fi

if [[ -n "${PID_BY_PORT}" ]]; then
  echo "Found PID by port: ${PID_BY_PORT}"
  kill "${PID_BY_PORT}" || true
  sleep 1
  if ps -p "${PID_BY_PORT}" >/dev/null 2>&1; then
    echo "Process still running, force killing..."
    kill -9 "${PID_BY_PORT}" || true
  fi
  echo "Stopped."
  exit 0
fi

# Fallback: find by process command keywords.
PID_BY_CMD=$(ps -ef | awk '/hr-system|HrSystemApplication|spring-boot/ && !/awk/ {print $2}' | head -n 1)
if [[ -n "${PID_BY_CMD}" ]]; then
  echo "Found PID by command: ${PID_BY_CMD}"
  kill "${PID_BY_CMD}" || true
  sleep 1
  if ps -p "${PID_BY_CMD}" >/dev/null 2>&1; then
    echo "Process still running, force killing..."
    kill -9 "${PID_BY_CMD}" || true
  fi
  echo "Stopped."
  exit 0
fi

echo "No running HR system process found."
