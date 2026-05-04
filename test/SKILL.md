# test-api.sh 调试指南

测试脚本 `test/test-api.sh` 的自包含集成测试：创建数据 → 验证 API → 自动清理。

## 快速诊断

```bash
# 运行完整测试
bash test/test-api.sh

# 查看失败的 API 具体返回了什么
bash test/test-api.sh 2>&1 | grep "✗ FAIL"

# 查看测试期间的后端日志
grep "$(date +%Y-%m-%d)" logs/hr-system.log | grep "ERROR\|Unhandled"
```

## 单接口手动测试

测试失败时，先拿到 token 再逐一手动测试失败的接口：

```bash
TOKEN=$(curl -s -X POST http://localhost:18080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# 替换为实际失败的接口
curl -s http://localhost:18080/api/attendance -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

## 常见失败模式与修复

| 错误关键字 | 根因 | 修复位置 |
|-----------|------|---------|
| `NULL not allowed for column "STATUS"` | 实体 NOT NULL 字段未设值 | Service 的 `apply()` 方法 |
| `ByteBuddyInterceptor` / `could not initialize proxy` | Jackson 无法序列化 Hibernate 代理 | 检查 `JacksonConfig` + `jackson-datatype-hibernate5-jakarta` 依赖 + `open-in-view` |
| `Referential integrity constraint violation` | 删除被其他表引用的行 | 调整删除顺序（先删子表再删父表），或设 `ON DELETE CASCADE` |
| `Numeric value out of range` | 数值类型精度不匹配（如 epoch millis 超出 INT） | 修改实体 `columnDefinition` |
| `DUPLICATE_RESOURCE` | 上一次测试的脏数据未清理 | 手动删 `data/hr.mv.db` 重跑 |
| `Table "xxx" not found` | Schema 变更后旧数据库文件与新实体不匹配 | 删 `data/hr.mv.db` 让 JPA 重建 |

## 数据库重置

当测试数据污染导致无法通过时：

```bash
rm -f data/hr.mv.db data/hr.trace.db
bash start.sh   # 自动重建并注入种子数据
```

## 日志分析

```bash
# 查看应用启动期间的所有错误
grep "ERROR" logs/hr-system.log | tail -20

# 查看特定时间段的 Unhandled error 及其堆栈
grep -A 5 "Unhandled error" logs/hr-system.log | tail -40

# 查看 LazyInitialization 相关错误
grep -B2 "could not initialize proxy" logs/hr-system.log
```

## 测试覆盖的接口

| 序号 | 接口 | 验证方式 |
|------|------|---------|
| 首页 | `GET /` | HTTP 200 |
| 认证 | `POST /api/auth/login` | 返回 token |
| 仪表盘 | `GET /api/dashboard/summary` | HTTP 200 |
| 组织 | `POST/GET /api/organizations`, `GET /api/organizations/position-tree` | CRUD + 树结构 |
| 员工 | `POST/GET/PUT /api/employees`, `POST /api/employees/resign`, `POST /api/employees/rehire` | CRUD + 状态变更 |
| 考勤 | `POST/GET /api/attendance`, `GET /api/attendance-rules` | CRUD |
| 薪资 | `POST/GET /api/salaries` | CRUD |
| 权限 | `GET /api/permissions`, `GET /api/permissions/roles` | 列表查询 |
| 验证 | 工号重复、邮箱格式、手机号格式 | 400/业务错误 |
| 清理 | `DELETE /api/employees/:id` | 级联删除关联数据 |

## 后台进程管理

```bash
# 检查应用是否运行
curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/

# 查看日志最近输出
tail -20 logs/hr-system.log

# 手动停止
pkill -f "hr-system"
```
