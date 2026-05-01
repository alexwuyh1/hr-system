#!/usr/bin/env bash
set -euo pipefail

# Stop the HR system process by port or by known keywords.
# Default port is 18080; override with PORT env var if needed.

PORT="${PORT:-18080}"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 状态提示函数
info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn()    { echo -e "${YELLOW}[!]${NC} $1"; }
error()   { echo -e "${RED}[✗]${NC} $1"; }

echo ""
info "正在停止 HR 系统 (端口: ${PORT})..."
echo ""

# Try to find PID by listening port first.
PID_BY_PORT=""
if command -v ss >/dev/null 2>&1; then
  PID_BY_PORT=$(ss -ltnp 2>/dev/null | awk -v p=":${PORT}" '$4 ~ p {print $NF}' | sed -n 's/.*pid=\\([0-9]*\\).*/\\1/p' | head -n 1)
fi

if [[ -n "${PID_BY_PORT}" ]]; then
  info "通过端口找到进程: PID ${PID_BY_PORT}"
  
  info "正在发送停止信号..."
  kill "${PID_BY_PORT}" || true
  
  # 等待进程退出
  for i in {1..5}; do
    sleep 1
    if ! ps -p "${PID_BY_PORT}" >/dev/null 2>&1; then
      success "进程已停止 (PID: ${PID_BY_PORT})"
      echo ""
      exit 0
    fi
    info "等待进程退出... ($i/5)"
  done
  
  warn "进程未响应，强制终止..."
  kill -9 "${PID_BY_PORT}" || true
  sleep 1
  
  if ! ps -p "${PID_BY_PORT}" >/dev/null 2>&1; then
    success "进程已强制终止 (PID: ${PID_BY_PORT})"
  else
    error "无法终止进程 (PID: ${PID_BY_PORT})"
    exit 1
  fi
  echo ""
  exit 0
fi

# Fallback: find by process command keywords.
PID_BY_CMD=$(ps -ef | awk '/hr-system|HrSystemApplication|spring-boot/ && !/awk/ && !/grep/ {print $2}' | head -n 1)
if [[ -n "${PID_BY_CMD}" ]]; then
  info "通过进程名找到: PID ${PID_BY_CMD}"
  
  info "正在发送停止信号..."
  kill "${PID_BY_CMD}" || true
  
  # 等待进程退出
  for i in {1..5}; do
    sleep 1
    if ! ps -p "${PID_BY_CMD}" >/dev/null 2>&1; then
      success "进程已停止 (PID: ${PID_BY_CMD})"
      echo ""
      exit 0
    fi
    info "等待进程退出... ($i/5)"
  done
  
  warn "进程未响应，强制终止..."
  kill -9 "${PID_BY_CMD}" || true
  sleep 1
  
  if ! ps -p "${PID_BY_CMD}" >/dev/null 2>&1; then
    success "进程已强制终止 (PID: ${PID_BY_CMD})"
  else
    error "无法终止进程 (PID: ${PID_BY_CMD})"
    exit 1
  fi
  echo ""
  exit 0
fi

# Check PID file
PID_FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/logs/hr-system.pid"
if [[ -f "${PID_FILE}" ]]; then
  PID_FROM_FILE=$(cat "${PID_FILE}")
  if ps -p "${PID_FROM_FILE}" >/dev/null 2>&1; then
    info "通过 PID 文件找到进程: PID ${PID_FROM_FILE}"
    info "正在发送停止信号..."
    kill "${PID_FROM_FILE}" || true
    sleep 2
    
    if ! ps -p "${PID_FROM_FILE}" >/dev/null 2>&1; then
      success "进程已停止 (PID: ${PID_FROM_FILE})"
      rm -f "${PID_FILE}"
      echo ""
      exit 0
    else
      warn "进程未响应，强制终止..."
      kill -9 "${PID_FROM_FILE}" || true
      success "进程已强制终止"
      rm -f "${PID_FILE}"
      echo ""
      exit 0
    fi
  else
    warn "PID 文件存在，但进程已不存在"
    rm -f "${PID_FILE}"
  fi
fi

echo ""
success "未找到运行中的 HR 系统进程"
echo ""
