# MVP 架构确立与迭代不偏移策略

## 更新说明
总结 MVP 阶段如何确立项目架构，以及在多次迭代中保持架构一致性的策略。针对 Agent 新对话丢失记忆、模型上下文窗口限制导致的重复实现和架构偏移问题。

---

## 一、根因分析

```
MVP 阶段
  ↓ 快速实现，架构模糊
多次迭代（不同对话）
  ↓ Agent 不记得之前的设计决策
  ↓ 每次重新理解代码
  ↓ 添加新功能时重复造轮子
架构偏移
  ↓ 双重认证、重复逻辑、风格不一致
```

**核心问题：**
- Agent 新对话丢失上下文
- 模型上下文窗口限制
- MVP 阶段未确立不可变层
- 缺乏代码级约束机制

---

## 二、MVP 阶段确立架构的核心原则

### 1. 先定义"不可变层"

MVP 阶段就要确定哪些是**后续不轻易改动的**：

```
不可变层（MVP 确定）
├── 认证机制（JWT vs Session）
├── 项目分层（Controller/Service/Repository）
├── 错误响应格式
├── 技术栈（Spring Security、JPA 等）
└── 代码规范（命名、包结构）

可变层（迭代中演进）
├── 业务逻辑
├── 数据模型
├── 前端 UI
└── 配置参数
```

**MVP 阶段只需要做 3 件事：**
1. 确定认证方式（写死在代码里，不要留"以后可以换"的余地）
2. 确定分层结构（创建空包结构）
3. 确定错误响应格式（定义 ErrorResponse 类）

---

### 2. 用代码约束代替文档

文档会过时，代码不会。

**反例（文档描述）：**
```markdown
# 认证规范
- 使用 JWT 认证
- Token 放在 Authorization header
```

**正例（代码约束）：**
```java
// 只保留一个认证入口
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // 所有认证逻辑集中在这里
}
```

**关键：让错误的实现方式"编译不通过"或"运行报错"**

---

### 3. 建立"单一入口"模式

每次迭代新增功能时，**强制通过已有的入口**：

```
新增功能
  ↓
先看现有代码如何处理类似问题
  ↓
复用现有模式（认证、错误处理、数据访问）
  ↓
不创建新的认证拦截器、新的错误格式
```

**具体做法：**
- 认证：只有 `JwtAuthenticationFilter` 处理
- 错误：只有 `GlobalExceptionHandler` 处理
- 数据访问：只有 `Repository` 层处理

---

## 三、多次迭代不偏移的实战策略

### 策略 1：先搜索，后实现

每次添加新功能前，先搜索现有代码：

```
需求：添加权限检查
  ↓
搜索：grep -r "permission" src/
  ↓
发现：已有 PermissionService
  ↓
复用，不创建新的 PermissionChecker
```

---

### 策略 2：删除优于重复

发现重复实现时，**立即删除旧的**：

```
发现 AuthInterceptor 和 JwtAuthenticationFilter 功能重叠
  ↓
不要"兼容两者"
  ↓
删除 AuthInterceptor，增强 JwtAuthenticationFilter
```

---

### 策略 3：小步验证

每次迭代后，**验证架构一致性**：

```
添加完新功能
  ↓
检查：
  - 认证是否走统一入口？
  - 错误响应格式是否一致？
  - 分层是否清晰？
  ↓
发现问题立即修复，不累积
```

---

## 四、Agent 对话场景下的特殊策略

### 1. 用代码注释代替记忆

```java
/**
 * 统一认证过滤器
 * 所有 API 认证都通过此过滤器，不要创建额外的拦截器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
```

---

### 2. 用测试用例约束行为

```java
@Test
void shouldReturnJsonError_WhenTokenInvalid() {
    // 确保认证失败返回 JSON，不是纯文本
}
```

---

### 3. 给 Agent 明确的"不要做"清单

```
需求：添加权限检查

不要做：
- 不要创建新的 AuthInterceptor
- 不要修改 SecurityConfig 的认证流程
- 不要返回纯文本错误

要做：
- 在 JwtAuthenticationFilter 中添加权限检查
- 使用 GlobalExceptionHandler 处理异常
- 返回 JSON 格式错误
```

---

## 五、MVP 架构检查清单

MVP 完成后，问自己 5 个问题：

| 问题 | 验收标准 |
|------|---------|
| 认证有几个入口？ | 只有 1 个 |
| 错误响应有几种格式？ | 只有 1 种（ErrorResponse） |
| 数据访问有几层？ | 只有 Repository 层 |
| 新增功能知道去哪加吗？ | 能明确说出包路径 |
| 删除一个文件会影响多少？ | 影响可预测 |

---

## 六、总结

```
MVP 阶段
  ↓ 确定不可变层（认证、分层、错误格式）
  ↓ 用代码约束，不用文档
  ↓ 建立单一入口

迭代阶段
  ↓ 先搜索现有实现
  ↓ 删除优于重复
  ↓ 小步验证架构一致性

Agent 场景
  ↓ 用注释代替记忆
  ↓ 用测试约束行为
  ↓ 给明确的"不要做"清单
```

**核心思想：让架构"自我约束"，而不是依赖记忆或文档。**
