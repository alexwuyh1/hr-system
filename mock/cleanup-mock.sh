#!/bin/bash
# HR System Mock 数据清理脚本
# 用法: bash mock/cleanup-mock.sh
# 安全: 仅清理 MOCK_ 前缀数据，不误伤正常数据

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:18080}"
TOKEN=""
DELETED_COUNT=0

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn()    { echo -e "${YELLOW}[!]${NC} $1"; }
error()   { echo -e "${RED}[✗]${NC} $1"; }

AUTH() { echo "Authorization: Bearer $TOKEN"; }

section() { echo -e "\n${YELLOW}=== $1 ===${NC}"; }

login() {
  info "登录获取 token..."
  local resp
  resp=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')
  TOKEN=$(echo "$resp" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  if [ -z "$TOKEN" ]; then
    error "登录失败"
    exit 1
  fi
  success "登录成功"
}

delete_employees() {
  section "清理员工及关联数据"

  local emp_resp
  emp_resp=$(curl -s "$BASE_URL/api/employees" -H "$(AUTH)")

  local mock_emp_nos
  mock_emp_nos=$(echo "$emp_resp" | grep -o '"employeeNo":"MOCK_[^"]*"' | cut -d'"' -f4 || true)

  if [ -z "$mock_emp_nos" ]; then
    info "无 Mock 员工数据"
    return
  fi

  info "清理关联数据..."
  for endpoint in attendance salaries; do
    local list_resp
    list_resp=$(curl -s "$BASE_URL/api/$endpoint" -H "$(AUTH)")
    local ids
    ids=$(echo "$list_resp" | grep -o '"id":[0-9]*' | cut -d: -f2 || true)
    for rid in $ids; do
      curl -s -X DELETE "$BASE_URL/api/$endpoint/$rid" -H "$(AUTH)" > /dev/null 2>&1 || true
    done
  done
  success "关联数据已清理"

  while IFS= read -r emp_no; do
    [ -z "$emp_no" ] && continue
    local emp_id
    emp_id=$(echo "$emp_resp" | grep -o "\"id\":[0-9]*,\"employeeNo\":\"$emp_no\"" | grep -o '"id":[0-9]*' | cut -d: -f2)
    if [ -z "$emp_id" ]; then
      warn "未找到员工 $emp_no 的 ID"
      continue
    fi
    curl -s -X DELETE "$BASE_URL/api/employees/$emp_id" -H "$(AUTH)" > /dev/null
    success "已删除员工: $emp_no (ID: $emp_id)"
    DELETED_COUNT=$((DELETED_COUNT + 1))
  done <<< "$mock_emp_nos"
}

cleanup_by_prefix() {
  local endpoint=$1
  local resp
  resp=$(curl -s "$BASE_URL$endpoint" -H "$(AUTH)")

  local names
  names=$(echo "$resp" | grep -o '"name":"MOCK_[^"]*"' | cut -d'"' -f4 || true)

  if [ -z "$names" ]; then
    return
  fi

  local count=0
  while IFS= read -r name; do
    [ -z "$name" ] && continue
    local item_id
    item_id=$(echo "$resp" | grep -o "\"id\":[0-9]*,\"name\":\"$name\"" | grep -o '"id":[0-9]*' | cut -d: -f2)
    if [ -n "$item_id" ]; then
      curl -s -X DELETE "$BASE_URL$endpoint/$item_id" -H "$(AUTH)" > /dev/null
      count=$((count + 1))
    fi
  done <<< "$names"

  if [ "$count" -gt 0 ]; then
    success "已清理 $endpoint: $count 条"
    DELETED_COUNT=$((DELETED_COUNT + count))
  fi
}

# ==================== 主流程 ====================
main() {
  echo -e "${BLUE}========================================${NC}"
  echo -e "${BLUE}  HR System Mock 数据清理${NC}"
  echo -e "${BLUE}  仅清理 MOCK_ 前缀数据${NC}"
  echo -e "${BLUE}========================================${NC}"

  login

  delete_employees

  section "清理组织数据"
  cleanup_by_prefix "/api/organizations"

  echo ""
  info "清理完成，共删除 $DELETED_COUNT 条 Mock 数据"
}

main
