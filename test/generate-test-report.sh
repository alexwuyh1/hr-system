#!/bin/bash
# HR System 生成 Markdown 测试报告
# 用法: bash test/generate-test-report.sh
# 输出: test/test-reports/report-YYYY-MM-DD-HHMMSS.md

set -euo pipefail

REPORT_DIR="$(dirname "$0")/test-reports"
mkdir -p "$REPORT_DIR"

REPORT_FILE="$REPORT_DIR/report-$(date +%Y-%m-%d-%H%M%S).md"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info() { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }

# 移除 ANSI 颜色代码
strip_colors() {
  sed 's/\x1b\[[0-9;]*m//g'
}

# ==================== 运行测试并收集结果 ====================
run_tests() {
  local test_output
  test_output=$(bash "$(dirname "$0")/test-api.sh" 2>&1 | strip_colors || true)
  
  # 提取统计信息
  local pass_count fail_count total_count
  pass_count=$(echo "$test_output" | grep -o "通过: [0-9]*" | grep -o "[0-9]*" || echo "0")
  fail_count=$(echo "$test_output" | grep -o "失败: [0-9]*" | grep -o "[0-9]*" || echo "0")
  total_count=$(echo "$test_output" | grep -o "总计: [0-9]*" | grep -o "[0-9]*" || echo "0")
  
  echo "$pass_count|$fail_count|$total_count"
}

# ==================== 检查编译 ====================
check_compile() {
  if mvn compile -q 2>&1 | grep -q "ERROR"; then
    echo "✗ 失败"
    return 1
  else
    echo "✓ 通过"
    return 0
  fi
}

# ==================== 检查前端访问 ====================
check_frontend() {
  local status
  status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:18080/" 2>/dev/null || echo "000")
  
  if [ "$status" = "200" ]; then
    echo "✓ HTTP 200"
    return 0
  else
    echo "✗ HTTP $status"
    return 1
  fi
}

# ==================== 检查日志错误 ====================
check_logs() {
  local log_file="logs/hr-system.log"
  
  if [ -f "$log_file" ]; then
    local error_count
    error_count=$(tail -n 100 "$log_file" | grep -c "ERROR" 2>/dev/null || true)
    echo "${error_count:-0}"
  else
    echo "0"
  fi
}

# ==================== 生成测试详情 ====================
generate_test_details() {
  local full_output
  full_output=$(bash "$(dirname "$0")/test-api.sh" 2>&1 | strip_colors || true)
  
  local current_section=""
  
  echo "$full_output" | while IFS= read -r line; do
    if echo "$line" | grep -q "==="; then
      # 章节标题（跳过汇总行）
      local section_name
      section_name=$(echo "$line" | sed 's/.*=== \(.*\) ===/\1/' | xargs)
      if [ -n "$section_name" ] && ! echo "$section_name" | grep -q "^[0-9]"; then
        continue
      fi
      if [ -n "$section_name" ]; then
        echo ""
        echo "### $section_name"
        echo ""
      fi
    elif echo "$line" | grep -q "✓ PASS"; then
      # 通过的测试
      local test_name
      test_name=$(echo "$line" | sed 's/.*✓ PASS //')
      echo "- [x] $test_name"
    elif echo "$line" | grep -q "✗ FAIL"; then
      # 失败的测试
      local test_name error_msg
      test_name=$(echo "$line" | sed 's/.*✗ FAIL //' | sed 's/ - .*//')
      error_msg=$(echo "$line" | sed 's/.*✗ FAIL.* - //')
      echo "- [ ] $test_name (失败: $error_msg)"
    fi
  done
}

# ==================== 生成 Markdown 报告 ====================
generate_report() {
  local test_result
  test_result=$(run_tests)
  
  local pass_count fail_count total_count
  pass_count=$(echo "$test_result" | cut -d'|' -f1)
  fail_count=$(echo "$test_result" | cut -d'|' -f2)
  total_count=$(echo "$test_result" | cut -d'|' -f3)
  
  local compile_status
  compile_status=$(check_compile)
  
  local frontend_status
  frontend_status=$(check_frontend)
  
  local log_errors
  log_errors=$(check_logs)
  
  # 生成报告
  cat > "$REPORT_FILE" << EOF
# HR System 测试报告

> 生成时间: $(date '+%Y-%m-%d %H:%M:%S')

## 测试摘要

| 指标 | 结果 |
|------|------|
| 编译 | $compile_status |
| API 测试 | $pass_count/$total_count 通过 |
| 前端访问 | $frontend_status |
| 日志错误 | $log_errors 个 ERROR |

## 测试详情
EOF

  # 添加测试用例详情
  generate_test_details >> "$REPORT_FILE"

  # 添加验收状态
  local compile_pass="✗"
  local api_pass="✗"
  local frontend_pass="✗"
  local log_pass="✗"
  
  [ "$compile_status" = "✓ 通过" ] && compile_pass="✓"
  [ "$fail_count" = "0" ] && api_pass="✓"
  [ "$frontend_status" = "✓ HTTP 200" ] && frontend_pass="✓"
  [ "$log_errors" = "0" ] && log_pass="✓"

  cat >> "$REPORT_FILE" << EOF

## 验收状态

| 验收项 | 状态 |
|--------|------|
| 编译无错误 | $compile_pass |
| API 100% 通过 | $api_pass |
| 前端正常加载 | $frontend_pass |
| 无 ERROR 日志 | $log_pass |

EOF

  # 如果全部通过，添加成功标记
  if [ "$fail_count" = "0" ] && [ "$compile_status" = "✓ 通过" ]; then
    echo "---" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "**测试结论: ✓ 全部通过**" >> "$REPORT_FILE"
  else
    echo "---" >> "$REPORT_FILE"
    echo "" >> "$REPORT_FILE"
    echo "**测试结论: ✗ 存在失败项，请查看错误报告**" >> "$REPORT_FILE"
  fi

  success "测试报告已生成: $REPORT_FILE"
}

# ==================== 主流程 ====================
main() {
  info "生成测试报告..."
  generate_report
  
  # 显示摘要
  echo ""
  echo -e "${YELLOW}====================${NC}"
  head -15 "$REPORT_FILE"
  echo -e "${YELLOW}...${NC}"
  echo ""
  info "完整报告: $REPORT_FILE"
}

main