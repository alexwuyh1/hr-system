#!/bin/bash
# HR System API 功能测试脚本
# 用法: bash test/test-api.sh

BASE_URL="http://localhost:18080"
PASS=0
FAIL=0
TOKEN=""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

pass() { echo -e "  ${GREEN}✓ PASS${NC} $1"; PASS=$((PASS + 1)); }
fail() { echo -e "  ${RED}✗ FAIL${NC} $1 - $2"; FAIL=$((FAIL + 1)); }
section() { echo -e "\n${YELLOW}=== $1 ===${NC}"; }

check_status() {
  local expected=$1 actual=$2 desc=$3
  if [ "$actual" = "$expected" ]; then
    pass "$desc (HTTP $actual)"
  else
    fail "$desc" "期望 HTTP $expected, 实际 HTTP $actual"
  fi
}

# ==================== 1. 首页 ====================
section "1. 首页访问"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/")
check_status 200 "$STATUS" "首页加载"

# ==================== 2. 认证 ====================
section "2. 认证模块"

LOGIN_RESP=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')
TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
if [ -n "$TOKEN" ]; then
  pass "管理员登录成功"
else
  fail "管理员登录" "无法获取 token: $LOGIN_RESP"
  exit 1
fi

AUTH_HEADER="Authorization: Bearer $TOKEN"

TEST_USER="testuser_$(date +%s)"
REG_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$TEST_USER\",\"password\":\"test123456\"}")
check_status 200 "$REG_RESP" "注册新用户"

# ==================== 3. 仪表盘 ====================
section "3. 仪表盘"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/dashboard/summary" \
  -H "$AUTH_HEADER")
check_status 200 "$STATUS" "获取仪表盘数据"

# ==================== 4. 组织配置 ====================
section "4. 组织配置"

ORG_RESP=$(curl -s -X POST "$BASE_URL/api/organizations" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d '{"name":"测试部门","type":"部门"}')
ORG_ID=$(echo "$ORG_RESP" | grep -o '"id":[0-9]*' | cut -d: -f2)
if [ -n "$ORG_ID" ]; then
  pass "创建组织成功 (ID: $ORG_ID)"
else
  fail "创建组织" "$ORG_RESP"
fi

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/organizations" \
  -H "$AUTH_HEADER")
check_status 200 "$STATUS" "获取组织列表"

# ==================== 5. 员工管理 ====================
section "5. 员工管理"

EMP_RESP=$(curl -s -X POST "$BASE_URL/api/employees" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d "{\"employeeNo\":\"TEST001\",\"name\":\"测试员工\",\"department\":\"测试部门\",\"title\":\"测试岗位\",\"hireDate\":\"2024-01-01\",\"status\":\"在职\",\"email\":\"test@test.com\",\"phone\":\"13800138000\"}")
EMP_ID=$(echo "$EMP_RESP" | grep -o '"id":[0-9]*' | cut -d: -f2)
if [ -n "$EMP_ID" ]; then
  pass "创建员工成功 (ID: $EMP_ID)"
else
  fail "创建员工" "$EMP_RESP"
fi

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/employees" \
  -H "$AUTH_HEADER")
check_status 200 "$STATUS" "获取员工列表"

if [ -n "$EMP_ID" ]; then
  UPDATE_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/api/employees/$EMP_ID" \
    -H "$AUTH_HEADER" -H "Content-Type: application/json" \
    -d "{\"employeeNo\":\"TEST001\",\"name\":\"测试员工更新\",\"department\":\"测试部门\",\"title\":\"测试岗位\",\"hireDate\":\"2024-01-01\",\"status\":\"在职\",\"email\":\"test@test.com\",\"phone\":\"13800138000\"}")
  check_status 200 "$UPDATE_RESP" "更新员工"
fi

if [ -n "$EMP_ID" ]; then
  RESIGN_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/employees/resign" \
    -H "$AUTH_HEADER" -H "Content-Type: application/json" \
    -d "{\"employeeNo\":\"TEST001\"}")
  check_status 200 "$RESIGN_RESP" "员工离职"
fi

if [ -n "$EMP_ID" ]; then
  REHIRE_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/employees/rehire" \
    -H "$AUTH_HEADER" -H "Content-Type: application/json" \
    -d "{\"employeeNo\":\"TEST001\"}")
  check_status 200 "$REHIRE_RESP" "员工复职"
fi

# ==================== 6. 考勤管理 ====================
section "6. 考勤管理"

ATT_RESP=$(curl -s -X POST "$BASE_URL/api/attendance" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d "{\"employeeId\":${EMP_ID:-1},\"workDate\":\"$(date +%Y-%m-%d)\",\"checkIn\":\"09:00:00\",\"checkOut\":\"18:00:00\"}")
ATT_ID=$(echo "$ATT_RESP" | grep -o '"id":[0-9]*' | cut -d: -f2)
if [ -n "$ATT_ID" ]; then
  pass "创建考勤记录成功 (ID: $ATT_ID)"
else
  fail "创建考勤记录" "$ATT_RESP"
fi

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/attendance" \
  -H "$AUTH_HEADER")
check_status 200 "$STATUS" "获取考勤列表"

# ==================== 7. 薪资管理 ====================
section "7. 薪资管理"

SAL_RESP=$(curl -s -X POST "$BASE_URL/api/salaries" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d "{\"employeeId\":${EMP_ID:-1},\"salaryMonth\":\"$(date +%Y-%m)\",\"baseSalary\":5000,\"bonus\":1000,\"deduction\":0}")
SAL_ID=$(echo "$SAL_RESP" | grep -o '"id":[0-9]*' | cut -d: -f2)
if [ -n "$SAL_ID" ]; then
  pass "创建薪资记录成功 (ID: $SAL_ID)"
else
  fail "创建薪资记录" "$SAL_RESP"
fi

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/salaries" \
  -H "$AUTH_HEADER")
check_status 200 "$STATUS" "获取薪资列表"

# ==================== 8. 权限管理 ====================
section "8. 权限管理"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/permissions/roles" \
  -H "$AUTH_HEADER")
check_status 200 "$STATUS" "获取角色列表"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/permissions" \
  -H "$AUTH_HEADER")
check_status 200 "$STATUS" "获取权限列表"

# ==================== 9. 报表 ====================
section "9. 报表"

STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/reports/summary" \
  -H "$AUTH_HEADER")
check_status 200 "$STATUS" "获取统计报表"

# ==================== 10. 数据验证 ====================
section "10. 数据验证"

DUP_RESP=$(curl -s -X POST "$BASE_URL/api/employees" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d "{\"employeeNo\":\"TEST001\",\"name\":\"重复员工\",\"department\":\"测试部门\",\"title\":\"测试岗位\",\"hireDate\":\"2024-01-01\",\"status\":\"在职\"}")
if echo "$DUP_RESP" | grep -q "已存在\|error\|message"; then
  pass "工号重复验证生效"
else
  fail "工号重复验证" "未检测到重复错误: $DUP_RESP"
fi

BAD_EMAIL=$(curl -s -X POST "$BASE_URL/api/employees" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d "{\"employeeNo\":\"TEST999\",\"name\":\"测试\",\"department\":\"测试部门\",\"title\":\"测试岗位\",\"hireDate\":\"2024-01-01\",\"status\":\"在职\",\"email\":\"invalid-email\"}")
if echo "$BAD_EMAIL" | grep -q "邮箱\|error\|message"; then
  pass "邮箱格式验证生效"
else
  fail "邮箱格式验证" "未检测到格式错误: $BAD_EMAIL"
fi

BAD_PHONE=$(curl -s -X POST "$BASE_URL/api/employees" \
  -H "$AUTH_HEADER" -H "Content-Type: application/json" \
  -d "{\"employeeNo\":\"TEST998\",\"name\":\"测试\",\"department\":\"测试部门\",\"title\":\"测试岗位\",\"hireDate\":\"2024-01-01\",\"status\":\"在职\",\"phone\":\"12345\"}")
if echo "$BAD_PHONE" | grep -q "手机\|error\|message"; then
  pass "手机号格式验证生效"
else
  fail "手机号格式验证" "未检测到格式错误: $BAD_PHONE"
fi

# ==================== 11. 清理测试数据 ====================
section "11. 清理测试数据"

if [ -n "$EMP_ID" ]; then
  ATT_LIST=$(curl -s "$BASE_URL/api/attendance" -H "$AUTH_HEADER")
  echo "$ATT_LIST" | grep -o "\"id\":[0-9]*" | while read line; do
    ATT_ID_CLEAN=$(echo "$line" | cut -d: -f2)
    curl -s -X DELETE "$BASE_URL/api/attendance/$ATT_ID_CLEAN" -H "$AUTH_HEADER" > /dev/null
  done
  
  SAL_LIST=$(curl -s "$BASE_URL/api/salaries" -H "$AUTH_HEADER")
  echo "$SAL_LIST" | grep -o "\"id\":[0-9]*" | while read line; do
    SAL_ID_CLEAN=$(echo "$line" | cut -d: -f2)
    curl -s -X DELETE "$BASE_URL/api/salaries/$SAL_ID_CLEAN" -H "$AUTH_HEADER" > /dev/null
  done
  
  curl -s -X DELETE "$BASE_URL/api/employees/$EMP_ID" -H "$AUTH_HEADER" > /dev/null
  pass "删除测试员工及关联数据"
fi

if [ -n "$ORG_ID" ]; then
  curl -s -X DELETE "$BASE_URL/api/organizations/$ORG_ID" -H "$AUTH_HEADER" > /dev/null
  pass "删除测试组织"
fi

# ==================== 汇总 ====================
echo -e "\n${YELLOW}====================${NC}"
echo -e "${GREEN}通过: $PASS${NC}"
echo -e "${RED}失败: $FAIL${NC}"
echo -e "${YELLOW}总计: $((PASS + FAIL))${NC}"

if [ $FAIL -gt 0 ]; then
  exit 1
fi
