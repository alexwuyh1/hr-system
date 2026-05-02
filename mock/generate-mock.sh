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

# 记录创建的 ID 用于验证
record_id() { CREATED_IDS="$CREATED_IDS $1"; }

# 从 JSON 响应中提取第一个 id 值
extract_id() { echo "$1" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2; }

# 登录获取 token
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

# 创建部门
create_departments() {
  section "创建部门"
  local dept_names=("${PREFIX}研发部${SUFFIX}" "${PREFIX}市场部${SUFFIX}" "${PREFIX}人事部${SUFFIX}")
  for name in "${dept_names[@]}"; do
    local resp
    resp=$(curl -s -X POST "$BASE_URL/api/departments" \
      -H "$(AUTH)" -H "Content-Type: application/json" \
      -d "{\"name\":\"$name\"}")
    local id
    id=$(extract_id "$resp")
    if [ -n "$id" ]; then
      success "创建部门: $name (ID: $id)"
      record_id "dept:$id"
    else
      error "创建部门失败: $name - $resp"
    fi
  done
}

# 创建岗位
create_positions() {
  section "创建岗位"
  local pos_names=("${PREFIX}工程师${SUFFIX}" "${PREFIX}经理${SUFFIX}" "${PREFIX}专员${SUFFIX}")
  for name in "${pos_names[@]}"; do
    local resp
    resp=$(curl -s -X POST "$BASE_URL/api/positions" \
      -H "$(AUTH)" -H "Content-Type: application/json" \
      -d "{\"name\":\"$name\"}")
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

# 创建职级
create_grades() {
  section "创建职级"
  local grades=("P5:5" "P6:6" "P7:7")
  for grade in "${grades[@]}"; do
    local name="${PREFIX}${grade%%:*}${SUFFIX}"
    local level="${grade##*:}"
    local resp
    resp=$(curl -s -X POST "$BASE_URL/api/grades" \
      -H "$(AUTH)" -H "Content-Type: application/json" \
      -d "{\"name\":\"$name\",\"level\":$level}")
    local id
    id=$(extract_id "$resp")
    if [ -n "$id" ]; then
      success "创建职级: $name (ID: $id)"
      record_id "grade:$id"
    else
      error "创建职级失败: $name - $resp"
    fi
  done
}

# 创建员工
create_employees() {
  section "创建员工"
  # 获取第一个部门、岗位、职级 ID
  local dept_id pos_id grade_id
  dept_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^dept:" | head -1 | cut -d: -f2)
  pos_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^pos:" | head -1 | cut -d: -f2)
  grade_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^grade:" | head -1 | cut -d: -f2)

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
      -d "{\"employeeNo\":\"$emp_no\",\"name\":\"$name\",\"department\":\"${PREFIX}研发部${SUFFIX}\",\"title\":\"${PREFIX}工程师${SUFFIX}\",\"departmentId\":${dept_id:-1},\"positionId\":${pos_id:-1},\"gradeId\":${grade_id:-1},\"hireDate\":\"2024-01-01\",\"status\":\"在职\",\"email\":\"$email\",\"phone\":\"$phone\"}")
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

# 创建考勤记录
create_attendance() {
  section "创建考勤记录"
  local emp_id
  emp_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^emp:" | head -1 | cut -d: -f2)
  if [ -z "$emp_id" ]; then
    warn "无员工 ID，跳过考勤记录"
    return
  fi

  local today
  today=$(date +%Y-%m-%d)
  local resp
  resp=$(curl -s -X POST "$BASE_URL/api/attendance" \
    -H "$(AUTH)" -H "Content-Type: application/json" \
    -d "{\"employeeId\":$emp_id,\"workDate\":\"$today\",\"checkIn\":\"09:00:00\",\"checkOut\":\"18:00:00\"}")
  local id
  id=$(extract_id "$resp")
  if [ -n "$id" ]; then
    success "创建考勤记录 (ID: $id)"
    record_id "att:$id"
  else
    error "创建考勤记录失败: $resp"
  fi
}

# 创建薪资记录
create_salary() {
  section "创建薪资记录"
  local emp_id
  emp_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^emp:" | head -1 | cut -d: -f2)
  if [ -z "$emp_id" ]; then
    warn "无员工 ID，跳过薪资记录"
    return
  fi

  local month
  month=$(date +%Y-%m)
  local resp
  resp=$(curl -s -X POST "$BASE_URL/api/salaries" \
    -H "$(AUTH)" -H "Content-Type: application/json" \
    -d "{\"employeeId\":$emp_id,\"salaryMonth\":\"$month\",\"baseSalary\":8000,\"bonus\":2000,\"deduction\":0}")
  local id
  id=$(extract_id "$resp")
  if [ -n "$id" ]; then
    success "创建薪资记录 (ID: $id)"
    record_id "sal:$id"
  else
    error "创建薪资记录失败: $resp"
  fi
}

# 创建请假记录
create_leaves() {
  section "创建请假记录"
  local emp_id
  emp_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^emp:" | head -1 | cut -d: -f2)
  if [ -z "$emp_id" ]; then
    warn "无员工 ID，跳过请假记录"
    return
  fi

  local today tomorrow
  today=$(date +%Y-%m-%d)
  tomorrow=$(date -d "+1 day" +%Y-%m-%d 2>/dev/null || date -v+1d +%Y-%m-%d 2>/dev/null || echo "2024-12-31")

  local resp
  resp=$(curl -s -X POST "$BASE_URL/api/leaves" \
    -H "$(AUTH)" -H "Content-Type: application/json" \
    -d "{\"employeeId\":$emp_id,\"startDate\":\"$today\",\"endDate\":\"$tomorrow\",\"type\":\"年假\",\"status\":\"APPROVED\",\"note\":\"${PREFIX}测试请假\"}")
  local id
  id=$(extract_id "$resp")
  if [ -n "$id" ]; then
    success "创建请假记录 (ID: $id)"
    record_id "leave:$id"
  else
    error "创建请假记录失败: $resp"
  fi
}

# 创建加班记录
create_overtime() {
  section "创建加班记录"
  local emp_id
  emp_id=$(echo "$CREATED_IDS" | tr ' ' '\n' | grep "^emp:" | head -1 | cut -d: -f2)
  if [ -z "$emp_id" ]; then
    warn "无员工 ID，跳过加班记录"
    return
  fi

  local today
  today=$(date +%Y-%m-%d)
  local resp
  resp=$(curl -s -X POST "$BASE_URL/api/overtime" \
    -H "$(AUTH)" -H "Content-Type: application/json" \
    -d "{\"employeeId\":$emp_id,\"workDate\":\"$today\",\"minutes\":180,\"status\":\"APPROVED\",\"note\":\"${PREFIX}测试加班\"}")
  local id
  id=$(extract_id "$resp")
  if [ -n "$id" ]; then
    success "创建加班记录 (ID: $id)"
    record_id "ot:$id"
  else
    error "创建加班记录失败: $resp"
  fi
}

# 验证数据
verify_data() {
  section "验证 Mock 数据"
  local pass=0 fail=0

  # 验证部门
  local depts
  depts=$(curl -s "$BASE_URL/api/departments" -H "$(AUTH)")
  local dept_count
  dept_count=$(echo "$depts" | grep -o '"name":"MOCK_[^"]*"' | wc -l)
  if [ "$dept_count" -ge 3 ]; then
    success "部门数据: $dept_count 条"
    pass=$((pass + 1))
  else
    error "部门数据不足: $dept_count 条"
    fail=$((fail + 1))
  fi

  # 验证员工
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

  echo ""
  info "验证结果: 通过 $pass, 失败 $fail"
  return $fail
}

section() { echo -e "\n${YELLOW}=== $1 ===${NC}"; }

# ==================== 主流程 ====================
main() {
  echo -e "${BLUE}========================================${NC}"
  echo -e "${BLUE}  HR System Mock 数据生成${NC}"
  echo -e "${BLUE}  前缀: $PREFIX | 后缀: $SUFFIX${NC}"
  echo -e "${BLUE}========================================${NC}"

  login
  create_departments
  create_positions
  create_grades
  create_employees
  create_attendance
  create_salary
  create_leaves
  create_overtime
  verify_data

  echo ""
  info "Mock 数据生成完成"
  info "清理命令: bash mock/cleanup-mock.sh"
}

main
