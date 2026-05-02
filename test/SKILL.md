---
name: "hr-auto-test"
description: "HR 系统自动测试修复工作流。实现新功能后自动编译、测试、修复、生成报告。Invoke when 实现新功能后需要验证，或测试失败需要自动修复时。"
---

# HR 自动测试修复

HR 系统新功能实现后的自测试修复流程。

## 何时触发

- Agent 实现了新功能，需要验证
- 测试失败，需要自动定位并修复
- 用户要求运行测试或生成测试报告

## 工作流程

```
实现功能 → 编译 → 重启 → 测试 → 通过 → 生成测试报告 ✓
                              → 失败 → 分析错误 → 修复 → 重测 (最多3轮)
```

## 快速执行

```bash
# 一键测试（含自动修复循环，通过后自动生成报告）
bash test/auto-test-fix.sh 3

# 单独运行测试
bash test/test-api.sh

# 单独生成测试报告
bash test/generate-test-report.sh

# 查看日志
tail -f logs/hr-system.log
```

## 测试脚本说明

| 脚本 | 用途 | 输出 |
|------|------|------|
| `test-api.sh` | API 集成测试（自包含测试数据） | 终端输出 PASS/FAIL |
| `auto-test-fix.sh` | 自动测试修复循环 | 终端输出 + 测试报告 |
| `generate-test-report.sh` | 生成 Markdown 测试报告 | `test/test-reports/report-*.md` |
| `generate-error-report.sh` | 生成 JSON 错误报告 | `logs/error-report-*.json` |

## 测试数据策略

`test-api.sh` 采用**自包含测试**模式：创建测试数据 → 执行测试 → 自动清理。无需预先生成 Mock 数据。

## 错误类型与修复方向

| error_type | 含义 | 修复方向 |
|-----------|------|---------|
| AUTH_ERROR | 认证/权限 | JWT、SecurityConfig、@PreAuthorize |
| VALIDATION_ERROR | 数据验证 | DTO 注解、验证器、请求格式 |
| NOT_FOUND | 接口不存在 | Controller @RequestMapping |
| SERVER_ERROR | 服务器内部 | Service 逻辑、Repository、空指针 |

## 自动修复规则

### 编译失败
1. 读取错误信息 → 2. 定位文件行号 → 3. 修复语法/导入 → 4. 重新编译 → 最多重试 3 次

### 测试失败
1. 读取失败详情 → 2. 查看 `logs/hr-system.log` → 3. 分类错误类型 → 4. 修复代码 → 5. 重启应用 → 6. 重新测试 → 最多重试 3 次

### 测试通过
1. 运行 `generate-test-report.sh` → 2. 报告保存到 `test/test-reports/` → 3. 报告命名 `report-YYYY-MM-DD-HHMMSS.md`

## 验收标准

| 指标 | 要求 |
|------|------|
| 编译 | 无错误 |
| API 测试 | 100% 通过 |
| 前端访问 | HTTP 200 |
| 日志 | 无 ERROR |
| 测试报告 | 已生成 |
