# Agent 自动测试修复工作流

> HR 系统新功能实现后的自测试修复流程

---

## 工作流程

```
┌─────────────┐
│ Agent 实现功能 │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  mvn compile │ ← 编译检查
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 重启应用     │ ← scripts/stop.sh + start.sh
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ 运行测试脚本  │ ← test-api.sh (自包含测试数据)
└──────┬──────┘
       │
       ├── 通过 ──→ ┌─────────────┐
       │            │ 生成测试报告 │ ← generate-test-report.sh
       │            └──────┬──────┘
       │                   │
       │                   ▼
       │            ┌─────────────┐
       │            │ Agent 读取报告│ ← 解析 Markdown
       │            └──────┬──────┘
       │                   │
       │                   ▼
       │            ┌─────────────┐
       │            │ 保存测试报告  │ ← test-reports/
       │            └─────────────┘
       │
       └── 失败 ──→ ┌─────────────┐
                    │ 生成错误报告 │ ← generate-error-report.sh
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ Agent 读取报告│ ← 解析 JSON
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ 定位并修复问题│ ← 根据 error_type 和 suggestion
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ 重新测试验证  │ ← 循环直到通过或超时
                    └─────────────┘
```

---

## 测试数据策略

### 为什么不需要独立的 Mock 数据？

`test-api.sh` 采用**自包含测试**模式：

```
创建测试数据 → 执行测试 → 清理测试数据
```

| 场景 | 旧方案 (Mock 数据) | 新方案 (自包含测试) |
|------|-------------------|-------------------|
| 员工列表查询 | 需预先生成 Mock 数据 | 测试中自动创建 |
| 薪资计算 | 需预先生成 Mock 数据 | 测试中自动创建 |
| 考勤统计 | 需预先生成 Mock 数据 | 测试中自动创建 |
| 数据清理 | 需运行 clean-mock-data.sh | 测试结束自动清理 |

### 测试数据覆盖

`test-api.sh` 覆盖的测试场景：

```
认证层:
  ├── 管理员登录
  └── 用户注册

组织配置:
  ├── 部门 CRUD
  ├── 岗位 CRUD
  └── 职级 CRUD

员工管理:
  ├── 员工创建/更新
  ├── 员工离职/复职
  └── 数据验证 (工号重复/邮箱/手机号)

业务模块:
  ├── 考勤记录 CRUD
  ├── 薪资记录 CRUD
  ├── 角色/权限查询
  └── 统计报表
```

---

## 错误报告格式

脚本生成 JSON 格式的错误报告，供 Agent 解析：

```json
{
  "timestamp": "2026-05-02T10:30:00+08:00",
  "summary": {
    "total": 20,
    "passed": 18,
    "failed": 2
  },
  "failures": [
    {
      "module": "创建员工",
      "detail": "期望 HTTP 200, 实际 HTTP 400",
      "error_type": "VALIDATION_ERROR",
      "suggestion": "检查 DTO 注解、验证器、请求格式"
    },
    {
      "module": "获取薪资列表",
      "detail": "期望 HTTP 200, 实际 HTTP 500",
      "error_type": "SERVER_ERROR",
      "suggestion": "检查 Service 层逻辑、数据库操作、空指针"
    }
  ],
  "app_logs": [
    "2026-05-02 10:30:25 ERROR Validation failed for class 'EmployeeRequest'",
    "2026-05-02 10:30:25 WARN  Email format invalid: test@invalid"
  ]
}
```

**Agent 解析规则：**

| error_type | 含义 | 修复方向 |
|-----------|------|---------|
| AUTH_ERROR | 认证/权限问题 | JWT 配置、SecurityConfig、@PreAuthorize |
| VALIDATION_ERROR | 数据验证失败 | DTO 注解、自定义验证器、请求格式 |
| NOT_FOUND | 接口不存在 | Controller @RequestMapping 路径 |
| SERVER_ERROR | 服务器内部错误 | Service 逻辑、Repository、空指针 |
| UNKNOWN | 未知错误 | 查看详细日志进一步分析 |

---

## 测试报告格式

验收通过后，Agent 生成 Markdown 格式的测试报告：

```markdown
# HR System 测试报告

> 生成时间: 2026-05-02 15:30:00

## 测试摘要

| 指标 | 结果 |
|------|------|
| 编译 | ✓ 通过 |
| API 测试 | 27/27 通过 |
| 前端访问 | ✓ HTTP 200 |
| 日志错误 | 0 个 ERROR |

## 测试详情

### 1. 首页访问
- [x] 首页加载 (HTTP 200)

### 2. 认证模块
- [x] 管理员登录成功
- [x] 注册新用户 (HTTP 200)

... (完整测试用例列表)

## 修复记录

| 时间 | 问题 | 修复方案 | 状态 |
|------|------|---------|------|
| 15:20 | 考勤规则创建失败 | POST 改为 PUT | ✓ 已修复 |
```

---

## 配置步骤

### 1. 测试目录结构

```
test/
  ├── README.md              # 本文件 - 工作流文档
  ├── test-api.sh            # API 集成测试 (自包含测试数据)
  ├── auto-test-fix.sh       # 自动测试修复循环
  ├── generate-error-report.sh  # 错误报告生成 (JSON)
  └── test-reports/          # 测试报告存储目录
```

### 2. Agent 实现新功能后的标准流程

```bash
# 步骤 1: 编译检查
mvn compile

# 步骤 2: 重启应用（如修改了 Java 代码）
bash scripts/stop.sh
bash scripts/start.sh
sleep 5

# 步骤 3: 运行自动测试修复
bash test/auto-test-fix.sh 3

# 步骤 4: 验收通过后生成测试报告
bash test/generate-test-report.sh
```

### 3. 失败处理策略

| 错误类型 | 自动修复动作 |
|---------|-------------|
| 编译错误 | 检查语法、导入、依赖 |
| HTTP 400 | 检查请求格式、验证规则 |
| HTTP 401/403 | 检查认证配置、权限设置 |
| HTTP 404 | 检查 Controller 路径映射 |
| HTTP 500 | 查看日志、检查业务逻辑 |
| 数据库错误 | 检查表结构、外键约束 |

---

## Agent 自动修复规则

### 规则 1: 编译失败

```
IF mvn compile 失败
THEN
  1. 读取错误信息
  2. 定位错误文件和行号
  3. 修复语法/导入问题
  4. 重新编译
  5. 最多重试 3 次
```

### 规则 2: 测试失败

```
IF test-api.sh 失败
THEN
  1. 读取失败项详情
  2. 查看应用日志 (logs/hr-system.log)
  3. 分类错误类型
  4. 根据错误类型修复代码
  5. 重启应用
  6. 重新测试
  7. 最多重试 3 次
```

### 规则 3: 数据验证失败

```
IF 数据验证错误
THEN
  1. 检查 DTO 注解 (@NotBlank, @Email 等)
  2. 检查自定义验证器 (@Phone, @UniqueEmployeeNo)
  3. 检查 Service 层业务校验
  4. 修复验证逻辑或测试数据
```

### 规则 4: 测试报告生成

```
IF test-api.sh 全部通过
THEN
  1. 收集测试结果
  2. 生成 Markdown 测试报告
  3. 保存到 test-reports/ 目录
  4. 报告命名: report-YYYY-MM-DD-HHMMSS.md
```

---

## 快速命令参考

```bash
# 一键测试（自动修复循环）
bash test/auto-test-fix.sh

# 详细测试（查看日志）
bash test/test-api.sh

# 生成错误报告（测试失败时）
bash test/generate-error-report.sh

# 生成测试报告（测试通过时）
bash test/generate-test-report.sh

# 查看实时日志
tail -f logs/hr-system.log

# 清理数据库重新开始
rm -f data/hr.db && bash scripts/start.sh
```

---

## 验收标准

| 指标 | 要求 |
|------|------|
| 编译 | 无错误 |
| API 测试 | 100% 通过 |
| 前端访问 | 页面正常加载 |
| 日志 | 无 ERROR 级别错误 |
| 测试报告 | 已生成并保存 |
