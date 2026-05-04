#!/bin/bash
# HR System Mock 数据生成脚本
# 用法: bash mock/generate-mock.sh
# 特点: 多次运行不冲突，自动验证，MOCK_ 前缀标识

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:18080}"
PREFIX="MOCK_"
SUFFIX="_$(date +%s)"
TOKEN=""
CREATED_IDS=""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn()    { echo -e "${YELLOW}[!]${NC} $1"; }
error()   { echo -e "${RED}[✗]${NC} $1"; }

record_id() { CREATED_IDS="$CREATED_IDS $1"; }
extract_id() { echo "$1" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2; }

login() {
  info "登录获取 token..."
  local resp
  resp=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')
  TOKEN=$(echo "$resp" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  if [ -z "$TOKEN" ]; then
    error "登录失败: $resp"
    exit 1
  fi
  success "登录成功"
}

AUTH() { echo "Authorization: Bearer $TOKEN"; }

section() { echo -e "\n${YELLOW}=== $1 ===${NC}"; }

# 创建组织（部门+职级+岗位）
create_organizations() {
  section "创建组织（部门+职级+岗位）"
  local dept_names=("${PREFIX}研发部${SUFFIX}" "${PREFIX}市场部${SUFFIX}" "${PREFIX}人事部${SUFFIX}")
  for name in "${dept_names[@]}"; do
    local resp
    resp=$(curl -s -X POST "$BASE_URL/api/organizations" \
      -H "$(AUTH)" -H "Content-Type: application/json" \
      -d "{\"name\":\"$name\",\"type\":\"部门\"}")
    local id
    id=$(extract_id "$resp")
    if [ -n "$id" ]; then
      success "创建部门: $name (ID: $id)"
      record_id "dept:$id"
    else
      error "创建部门失败: $name - $resp"
    fi
  done

  local grade_names=("${PREFIX}P3${SUFFIX}" "${PREFIX}P4${SUFFIX}")
  for name in "${grade_names[@]}"; do
    local resp
    resp=$(curl -s -X POST "$BASE_URL/api/organizations" \
      -H "$(AUTH)" -H "Content-Type: application/json" \
      -d "{\"name\":\"$name\",\"type\":\"职级\"}")
    local id
    id=$(extract_id "$resp")
    if [ -n "$id" ]; then
      success "创建职级: $name (ID: $id)"
      record_id "grade:$id"
    else
      error "创建职级失败: $name - $resp"
    fi
  done

  local dept_id
  dept_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^dept:" | head -1 | cut -d: -f2)
  local grade_id
  grade_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^grade:" | head -1 | cut -d: -f2)
  local pos_names=("${PREFIX}工程师${SUFFIX}" "${PREFIX}经理${SUFFIX}")
  for name in "${pos_names[@]}"; do
    local resp
    resp=$(curl -s -X POST "$BASE_URL/api/organizations" \
      -H "$(AUTH)" -H "Content-Type: application/json" \
      -d "{\"name\":\"$name\",\"type\":\"岗位\",\"parentId\":$dept_id,\"gradeId\":$grade_id}")
    local id
    id=$(extract_id "$resp")
    if [ -n "$id" ]; then
      success "创建岗位: $name (ID: $id)"
      record_id "pos:$id"
    else
      error "创建岗位失败: $name - $resp"
    fi
  done
}

# 创建员工
create_employees() {
  section "创建员工"
  local pos_id
  pos_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^pos:" | head -1 | cut -d: -f2)

  local employees=(
    "张三:MOCK_EMP001${SUFFIX}:zhangsan@mock.com:13800000001"
    "李四:MOCK_EMP002${SUFFIX}:lisi@mock.com:13800000002"
    "王五:MOCK_EMP003${SUFFIX}:wangwu@mock.com:13800000003"
  )
  for emp in "${employees[@]}"; do
    IFS=':' read -r name emp_no email phone <<< "$emp"
    local resp
    resp=$(curl -s -X POST "$BASE_URL/api/employees" \
      -H "$(AUTH)" -H "Content-Type: application/json" \
      -d "{\"employeeNo\":\"$emp_no\",\"name\":\"$name\",\"positionId\":$pos_id,\"hireDate\":\"2024-01-01\",\"status\":\"在职\",\"email\":\"$email\",\"phone\":\"$phone\"}")
    local id
    id=$(extract_id "$resp")
    if [ -n "$id" ]; then
      success "创建员工: $name (ID: $id)"
      record_id "emp:$id"
    else
      error "创建员工失败: $name - $resp"
    fi
  done
}

# 创建考勤记录（每个员工 3 天）
create_attendance() {
  section "创建考勤记录"
  local emp_ids
  emp_ids=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^emp:" | cut -d: -f2)
  if [ -z "$emp_ids" ]; then
    warn "无员工 ID，跳过考勤记录"
    return
  fi

  local today yesterday day_before
  today=$(date +%Y-%m-%d)
  yesterday=$(date -d "-1 day" +%Y-%m-%d 2>/dev/null || date -v-1d +%Y-%m-%d 2>/dev/null || echo "2024-12-30")
  day_before=$(date -d "-2 days" +%Y-%m-%d 2>/dev/null || date -v-2d +%Y-%m-%d 2>/dev/null || echo "2024-12-29")
  local dates=("$today" "$yesterday" "$day_before")

  for emp_id in $emp_ids; do
    for work_date in "${dates[@]}"; do
      local resp
      resp=$(curl -s -X POST "$BASE_URL/api/attendance" \
        -H "$(AUTH)" -H "Content-Type: application/json" \
        -d "{\"employeeId\":$emp_id,\"workDate\":\"$work_date\",\"checkIn\":\"09:00:00\",\"checkOut\":\"18:00:00\"}")
      local id
      id=$(extract_id "$resp")
      if [ -n "$id" ]; then
        success "创建考勤: 员工 $emp_id, 日期 $work_date (ID: $id)"
        record_id "att:$id"
      else
        error "创建考勤失败: $resp"
      fi
    done
  done
}

# 创建薪资记录（每个员工 3 个月）
create_salary() {
  section "创建薪资记录"
  local emp_ids
  emp_ids=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^emp:" | cut -d: -f2)
  if [ -z "$emp_ids" ]; then
    warn "无员工 ID，跳过薪资记录"
    return
  fi

  local m1 m2 m3
  m1=$(date +%Y-%m)
  m2=$(date -d "-1 month" +%Y-%m 2>/dev/null || date -v-1m +%Y-%m 2>/dev/null || echo "2024-11")
  m3=$(date -d "-2 months" +%Y-%m 2>/dev/null || date -v-2m +%Y-%m 2>/dev/null || echo "2024-10")
  local months=("$m1" "$m2" "$m3")

  for emp_id in $emp_ids; do
    for month in "${months[@]}"; do
      local resp
      resp=$(curl -s -X POST "$BASE_URL/api/salaries" \
        -H "$(AUTH)" -H "Content-Type: application/json" \
        -d "{\"employeeId\":$emp_id,\"salaryMonth\":\"$month\",\"baseSalary\":8000,\"bonus\":2000,\"deduction\":0}")
      local id
      id=$(extract_id "$resp")
      if [ -n "$id" ]; then
        success "创建薪资: 员工 $emp_id, 月份 $month (ID: $id)"
        record_id "sal:$id"
      else
        error "创建薪资失败: $resp"
      fi
    done
  done
}

# 验证数据
verify_data() {
  section "验证 Mock 数据"
  local pass=0 fail=0

  local orgs
  orgs=$(curl -s "$BASE_URL/api/organizations" -H "$(AUTH)")
  local org_count
  org_count=$(echo "$orgs" | grep -o '"name":"MOCK_[^"]*"' | wc -l)
  if [ "$org_count" -ge 5 ]; then
    success "组织数据: $org_count 条"
    pass=$((pass + 1))
  else
    error "组织数据不足: $org_count 条"
    fail=$((fail + 1))
  fi

  local emps
  emps=$(curl -s "$BASE_URL/api/employees" -H "$(AUTH)")
  local emp_count
  emp_count=$(echo "$emps" | grep -o '"employeeNo":"MOCK_[^"]*"' | wc -l)
  if [ "$emp_count" -ge 3 ]; then
    success "员工数据: $emp_count 条"
    pass=$((pass + 1))
  else
    error "员工数据不足: $emp_count 条"
    fail=$((fail + 1))
  fi

  local atts
  atts=$(curl -s "$BASE_URL/api/attendance" -H "$(AUTH)")
  local att_count
  att_count=$(echo "$atts" | grep -o '"id":[0-9]*' | wc -l)
  local expected_att=$((emp_count * 3))
  if [ "$att_count" -ge "$expected_att" ]; then
    success "考勤数据: $att_count 条 (预期 >= $expected_att)"
    pass=$((pass + 1))
  else
    error "考勤数据不足: $att_count 条 (预期 >= $expected_att)"
    fail=$((fail + 1))
  fi

  local sals
  sals=$(curl -s "$BASE_URL/api/salaries" -H "$(AUTH)")
  local sal_count
  sal_count=$(echo "$sals" | grep -o '"id":[0-9]*' | wc -l)
  local expected_sal=$((emp_count * 3))
  if [ "$sal_count" -ge "$expected_sal" ]; then
    success "薪资数据: $sal_count 条 (预期 >= $expected_sal)"
    pass=$((pass + 1))
  else
    error "薪资数据不足: $sal_count 条 (预期 >= $expected_sal)"
    fail=$((fail + 1))
  fi

  echo ""
  info "验证结果: 通过 $pass, 失败 $fail"
  return $fail
}

# ==================== 主流程 ====================
main() {
  echo -e "${BLUE}========================================${NC}"
  echo -e "${BLUE}  HR System Mock 数据生成${NC}"
  echo -e "${BLUE}  前缀: $PREFIX | 后缀: $SUFFIX${NC}"
  echo -e "${BLUE}========================================${NC}"

  login
  create_organizations
  create_employees
  create_attendance
  create_salary
  verify_data

  echo ""
  info "Mock 数据生成完成"
  info "清理命令: bash mock/cleanup-mock.sh"
}

main
