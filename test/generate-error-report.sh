#!/bin/bash
# HR System 生成 Agent 可读的错误报告
# 用法: bash test/generate-error-report.sh
# 输出: logs/error-report.json

set -euo pipefail

LOG_DIR="logs"
REPORT_FILE="$LOG_DIR/error-report.json"

mkdir -p "$LOG_DIR"

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info() { echo -e "${BLUE}[INFO]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ==================== 运行测试并收集结果 ====================
run_tests() {
  local test_output
  test_output=$(bash "$(dirname "$0")/test-api.sh" 2>&1 || true)
  
  # 提取统计信息
  local pass_count fail_count
  pass_count=$(echo "$test_output" | grep -o "通过: [0-9]*" | grep -o "[0-9]*" || echo "0")
  fail_count=$(echo "$test_output" | grep -o "失败: [0-9]*" | grep -o "[0-9]*" || echo "0")
  
  # 提取失败项
  local failures
  failures=$(echo "$test_output" | grep "✗ FAIL" || true)
  
  echo "$pass_count|$fail_count|$failures"
}

# ==================== 收集应用日志 ====================
collect_app_logs() {
  local log_file="logs/hr-system.log"
  
  if [ -f "$log_file" ]; then
    # 提取最近错误日志
    tail -n 100 "$log_file" | grep -E "ERROR|WARN|Exception|Failed" | tail -n 20 || echo ""
  else
    echo ""
  fi
}

# ==================== 生成 JSON 报告 ====================
generate_report() {
  local test_result
  test_result=$(run_tests)
  
  local pass_count fail_count failures
  pass_count=$(echo "$test_result" | cut -d'|' -f1)
  fail_count=$(echo "$test_result" | cut -d'|' -f2)
  failures=$(echo "$test_result" | cut -d'|' -f3-)
  
  local app_logs
  app_logs=$(collect_app_logs)
  
  # 构建 JSON
  cat > "$REPORT_FILE" << EOF
{
  "timestamp": "$(date -Iseconds)",
  "summary": {
    "total": $((pass_count + fail_count)),
    "passed": $pass_count,
    "failed": $fail_count
  },
  "failures": [
EOF

  # 添加失败项
  local first=true
  if [ -n "$failures" ]; then
    echo "$failures" | while read -r line; do
      local module detail error_type suggestion
      
      # 解析失败信息
      module=$(echo "$line" | sed 's/.*FAIL //' | cut -d'-' -f1 | xargs)
      detail=$(echo "$line" | sed 's/.*- //')
      
      # 分类错误
      if echo "$detail" | grep -q "HTTP 401\|HTTP 403"; then
        error_type="AUTH_ERROR"
        suggestion="检查 JWT 配置、SecurityConfig、权限注解"
      elif echo "$detail" | grep -q "HTTP 400"; then
        error_type="VALIDATION_ERROR"
        suggestion="检查 DTO 注解、验证器、请求格式"
      elif echo "$detail" | grep -q "HTTP 404"; then
        error_type="NOT_FOUND"
        suggestion="检查 Controller @RequestMapping 路径"
      elif echo "$detail" | grep -q "HTTP 500"; then
        error_type="SERVER_ERROR"
        suggestion="检查 Service 层逻辑、数据库操作、空指针"
      else
        error_type="UNKNOWN"
        suggestion="查看详细日志进一步分析"
      fi
      
      if [ "$first" = true ]; then
        first=false
      else
        echo "," >> "$REPORT_FILE"
      fi
      
      cat >> "$REPORT_FILE" << EOF
    {
      "module": "$module",
      "detail": "$detail",
      "error_type": "$error_type",
      "suggestion": "$suggestion"
    }
EOF
    done
  fi

  cat >> "$REPORT_FILE" << EOF

  ],
  "app_logs": [
EOF

  # 添加应用日志
  if [ -n "$app_logs" ]; then
    first=true
    echo "$app_logs" | while read -r line; do
      if [ "$first" = true ]; then
        first=false
      else
        echo "," >> "$REPORT_FILE"
      fi
      echo "    \"$(echo "$line" | sed 's/"/\\"/g')\"" >> "$REPORT_FILE"
    done
  fi

  cat >> "$REPORT_FILE" << EOF

  ]
}
EOF

  info "错误报告已生成: $REPORT_FILE"
}

# ==================== 主流程 ====================
main() {
  info "生成 Agent 可读错误报告..."
  generate_report
  
  # 显示摘要
  local fail_count
  fail_count=$(grep -o '"failed": [0-9]*' "$REPORT_FILE" | grep -o '[0-9]*')
  
  if [ "$fail_count" -gt 0 ]; then
    error "发现 $fail_count 个失败项"
    info "请 Agent 读取 $REPORT_FILE 进行问题定位和修复"
  else
    echo -e "${GREEN}[✓]${NC} 所有测试通过"
  fi
}

main
