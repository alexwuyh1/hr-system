#!/bin/bash
# HR System 自动测试与修复脚本
# 用法: bash test/auto-test-fix.sh [MAX_RETRIES]
# 示例: bash test/auto-test-fix.sh 3

set -euo pipefail

MAX_RETRIES="${1:-3}"
BASE_URL="${BASE_URL:-http://localhost:18080}"
PASS=0
FAIL=0
RETRY_COUNT=0

# 颜色输出
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
success() { echo -e "${GREEN}[✓]${NC} $1"; }
warn()    { echo -e "${YELLOW}[!]${NC} $1"; }
error()   { echo -e "${RED}[✗]${NC} $1"; }

# ==================== 检查应用状态 ====================
check_app_running() {
  info "检查应用状态..."
  if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/" | grep -q "200"; then
    success "应用运行正常"
    return 0
  else
    error "应用未响应"
    return 1
  fi
}

# ==================== 运行测试 ====================
run_tests() {
  info "运行 API 测试..."
  
  local test_output
  local test_exit_code=0
  
  test_output=$(bash "$(dirname "$0")/test-api.sh" 2>&1) || test_exit_code=$?
  
  echo "$test_output"
  
  # 提取测试结果
  PASS=$(echo "$test_output" | grep -o "通过: [0-9]*" | grep -o "[0-9]*" || echo "0")
  FAIL=$(echo "$test_output" | grep -o "失败: [0-9]*" | grep -o "[0-9]*" || echo "0")
  
  return $test_exit_code
}

# ==================== 分析错误 ====================
analyze_errors() {
  info "分析测试失败原因..."
  
  # 运行测试并捕获详细输出
  local test_output
  test_output=$(bash "$(dirname "$0")/test-api.sh" 2>&1 || true)
  
  # 提取失败项
  local failures
  failures=$(echo "$test_output" | grep "✗ FAIL" || true)
  
  if [ -n "$failures" ]; then
    error "发现以下失败项:"
    echo "$failures" | while read -r line; do
      echo "  $line"
    done
    
    # 生成结构化错误报告（供 Agent 使用）
    local error_report="logs/error-report-$(date +%Y%m%d-%H%M%S).json"
    mkdir -p logs
    
    echo "{" > "$error_report"
    echo "  \"timestamp\": \"$(date -Iseconds)\"," >> "$error_report"
    echo "  \"failures\": [" >> "$error_report"
    
    local first=true
    echo "$failures" | while read -r line; do
      local module=$(echo "$line" | grep -o "FAIL [^-]*" | sed 's/FAIL //' | xargs)
      local detail=$(echo "$line" | sed 's/.*- //')
      
      if [ "$first" = true ]; then
        first=false
      else
        echo "," >> "$error_report"
      fi
      
      echo "    {\"module\": \"$module\", \"detail\": \"$detail\"}" >> "$error_report"
    done
    
    echo "  ]," >> "$error_report"
    echo "  \"log_file\": \"logs/hr-system.log\"" >> "$error_report"
    echo "}" >> "$error_report"
    
    info "错误报告已保存: $error_report"
    
    # 分类错误类型
    if echo "$failures" | grep -q "HTTP 401\|HTTP 403"; then
      warn "认证相关错误 - 检查 JWT 配置或权限设置"
    fi
    
    if echo "$failures" | grep -q "HTTP 400\|验证"; then
      warn "数据验证错误 - 检查输入格式或验证规则"
    fi
    
    if echo "$failures" | grep -q "HTTP 500"; then
      warn "服务器内部错误 - 检查后端代码逻辑"
    fi
    
    if echo "$failures" | grep -q "HTTP 404"; then
      warn "接口不存在 - 检查 Controller 映射"
    fi
  fi
  
  return 1
}

# ==================== 查看日志 ====================
show_recent_logs() {
  local log_file="logs/hr-system.log"
  
  if [ -f "$log_file" ]; then
    info "最近应用日志:"
    echo "----------------------------------------"
    tail -n 30 "$log_file" | grep -E "ERROR|WARN|Exception" || echo "无错误日志"
    echo "----------------------------------------"
  fi
}

# ==================== 修复建议 ====================
suggest_fixes() {
  info "修复建议:"
  echo ""
  echo "1. 检查代码编译:"
  echo "   mvn compile"
  echo ""
  echo "2. 检查特定模块:"
  echo "   mvn compile -pl . -am"
  echo ""
  echo "3. 重启应用:"
  echo "   bash test/stop.sh"
  echo "   bash test/start.sh"
  echo ""
  echo "4. 清理数据库重新测试:"
  echo "   rm -f data/hr.db"
  echo "   bash scripts/start.sh"
  echo ""
  echo "5. 手动测试失败接口:"
  echo "   curl -v http://localhost:18080/api/xxx"
}

# ==================== 主流程 ====================
main() {
  echo -e "${BLUE}========================================${NC}"
  echo -e "${BLUE}  HR System 自动测试与修复${NC}"
  echo -e "${BLUE}========================================${NC}"
  echo ""
  
  # 检查应用
  if ! check_app_running; then
    error "应用未运行，请先启动应用"
    info "bash scripts/start.sh"
    exit 1
  fi
  
  echo ""
  
  # 运行测试循环
  while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo -e "\n${YELLOW}=== 第 $RETRY_COUNT 轮测试 (最大 $MAX_RETRIES 轮) ===${NC}\n"
    
    if run_tests; then
      echo ""
      success "所有测试通过!"
      info "通过: $PASS | 失败: $FAIL"
      
      # 生成测试报告
      echo ""
      info "生成测试报告..."
      bash "$(dirname "$0")/generate-test-report.sh"
      
      exit 0
    else
      echo ""
      error "测试失败"
      info "通过: $PASS | 失败: $FAIL"
      
      if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
        warn "准备下一轮重试..."
        analyze_errors
        show_recent_logs
        sleep 2
      fi
    fi
  done
  
  # 所有重试失败
  echo -e "\n${RED}========================================${NC}"
  error "经过 $MAX_RETRIES 轮测试，仍有 $FAIL 项失败"
  echo -e "${RED}========================================${NC}"
  
  echo ""
  analyze_errors
  echo ""
  show_recent_logs
  echo ""
  suggest_fixes
  
  exit 1
}

main
