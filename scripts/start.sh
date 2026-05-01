#!/usr/bin/env bash
set -euo pipefail

# Start the HR system in the background and write a PID file.
# Default port is 18080; override with PORT env var if needed.

PORT="${PORT:-18080}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
LOG_FILE="${ROOT_DIR}/logs/hr-system.log"
PID_FILE="${ROOT_DIR}/logs/hr-system.pid"

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
info "正在启动 HR 系统..."
echo ""

# 检查端口是否被占用
if command -v ss >/dev/null 2>&1; then
  if ss -ltnp | grep -q ":${PORT} "; then
    warn "端口 ${PORT} 已被占用"
    info "尝试自动清理..."
    "${SCRIPT_DIR}/stop.sh"
    success "端口已清理"
    echo ""
  fi
fi

# 检查是否已在运行
mkdir -p "${ROOT_DIR}/logs"

if [[ -f "${PID_FILE}" ]] && ps -p "$(cat "${PID_FILE}")" >/dev/null 2>&1; then
  warn "HR 系统已在运行 (PID: $(cat "${PID_FILE}"))"
  info "如需重启，请先运行: ./scripts/stop.sh"
  echo ""
  exit 0
fi

# 清理旧的 PID 文件
[[ -f "${PID_FILE}" ]] && rm -f "${PID_FILE}"

info "项目目录: ${ROOT_DIR}"
info "日志文件: ${LOG_FILE}"
info "运行端口: ${PORT}"
echo ""

# 启动应用
info "正在启动 Spring Boot 应用..."
(
  cd "${ROOT_DIR}"
  # Ensure JAVA_HOME for a clean environment.
  export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
  export PATH="${JAVA_HOME}/bin:${PATH}"
  mvn -q -DskipTests spring-boot:run -Dspring-boot.run.arguments="--server.port=${PORT}" \
    > "${LOG_FILE}" 2>&1 &
  echo $! > "${PID_FILE}"
)

# 等待启动
info "等待应用启动..."
for i in {1..15}; do
  sleep 1
  
  # 检查进程是否存在
  if [[ -f "${PID_FILE}" ]] && ! ps -p "$(cat "${PID_FILE}")" >/dev/null 2>&1; then
    echo ""
    error "应用启动失败"
    info "请查看日志: ${LOG_FILE}"
    echo ""
    info "最近 10 行日志:"
    tail -n 10 "${LOG_FILE}" 2>/dev/null || true
    echo ""
    exit 1
  fi
  
  # 检查端口是否已监听
  if command -v ss >/dev/null 2>&1 && ss -ltnp | grep -q ":${PORT} "; then
    echo ""
    success "HR 系统启动成功!"
    info "PID: $(cat "${PID_FILE}")"
    info "端口: ${PORT}"
    info "访问地址: http://localhost:${PORT}"
    info "日志文件: ${LOG_FILE}"
    echo ""
    exit 0
  fi
  
  # 显示进度
  if [[ $((i % 3)) -eq 0 ]]; then
    info "启动中... ($i/15)"
  fi
done

echo ""
warn "应用可能仍在启动中，请稍后检查"
info "PID: $(cat "${PID_FILE}" 2>/dev/null || echo '未知')"
info "日志文件: ${LOG_FILE}"
info "查看日志: tail -f ${LOG_FILE}"
echo ""
