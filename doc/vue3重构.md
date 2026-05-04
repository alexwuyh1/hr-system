# HR 管理系统前端 Vue 3 重构方案

## 更新内容

本文档规划将现有基于原生 JavaScript + 手动 DOM 操作的前端架构迁移至 Vue 3 组合式 API 架构，提升代码可维护性、组件复用率和开发效率。

---

## 一、现状分析

### 当前架构

项目采用原生 JavaScript 实现，代码组织方式如下：

- **入口文件**：[index.html](file:///home/alex/hr-system/src/main/resources/static/index.html) 加载所有脚本
- **核心模块**：[core/api.js](file:///home/alex/hr-system/src/main/resources/static/core/api.js)、[core/auth.js](file:///home/alex/hr-system/src/main/resources/static/core/auth.js)、[core/tabs.js](file:///home/alex/hr-system/src/main/resources/static/core/tabs.js)、[core/utils.js](file:///home/alex/hr-system/src/main/resources/static/core/utils.js)、[core/modal.js](file:///home/alex/hr-system/src/main/resources/static/core/modal.js)
- **业务模块**：[modules/employees.js](file:///home/alex/hr-system/src/main/resources/static/modules/employees.js)、[modules/attendance.js](file:///home/alex/hr-system/src/main/resources/static/modules/attendance.js)、[modules/salary.js](file:///home/alex/hr-system/src/main/resources/static/modules/salary.js)、[modules/org.js](file:///home/alex/hr-system/src/main/resources/static/modules/org.js)、[modules/permissions.js](file:///home/alex/hr-system/src/main/resources/static/modules/permissions.js)、[modules/dashboard.js](file:///home/alex/hr-system/src/main/resources/static/modules/dashboard.js)、[modules/import-export.js](file:///home/alex/hr-system/src/main/resources/static/modules/import-export.js)
- **HTML 模板**：通过 [tabs/*.html](file:///home/alex/hr-system/src/main/resources/static/tabs/) 按需加载

### 主要问题

1. **DOM 操作分散**：每个模块使用 `document.createElement`、`innerHTML` 手动构建 UI，代码冗长且易出错
2. **状态管理混乱**：全局变量 `initCache`、`authToken`、`selectedRole` 等散布在各模块，状态同步困难
3. **组件复用困难**：表单、表格、模态框等通用 UI 缺乏封装，每个模块重复实现
4. **响应式缺失**：数据更新后需手动调用 `apply*` 函数刷新视图，容易遗漏
5. **类型安全不足**：缺乏类型检查，API 响应数据结构不明确

---

## 二、重构目标

### 技术栈

- **Vue 3**：使用组合式 API（Composition API）和 `<script setup>` 语法
- **Vite**：作为构建工具，提供快速开发和生产构建
- **Vue Router**：替代现有的 tab 切换逻辑
- **Pinia**：状态管理，替代全局变量
- **Element Plus**：UI 组件库（可选，基于项目现有样式风格决定）

### 核心收益

1. **声明式渲染**：模板语法替代手动 DOM 操作，代码量减少约 60%
2. **响应式状态**：数据变更自动更新视图，无需手动调用刷新函数
3. **组件化架构**：通用组件（表格、表单、模态框）可复用
4. **类型安全**：配合 TypeScript 提供完整的类型检查
5. **开发体验**：Vite 热更新、Vue DevTools 调试支持

---

## 三、架构设计

### 目录结构

```
src/main/resources/static/
├── index.html                    # Vue 应用入口
├── vite.config.js                # Vite 配置
├── package.json                  # 依赖管理
├── src/
│   ├── main.js                   # Vue 应用初始化
│   ├── App.vue                   # 根组件
│   ├── api/
│   │   ├── index.js              # API 基础配置
│   │   ├── auth.js               # 认证相关 API
│   │   ├── employees.js          # 员工管理 API
│   │   ├── attendance.js         # 考勤管理 API
│   │   ├── salary.js             # 薪资管理 API
│   │   ├── organization.js       # 组织管理 API
│   │   └── permissions.js        # 权限管理 API
│   ├── stores/
│   │   ├── auth.js               # 认证状态
│   │   ├── employees.js          # 员工状态
│   │   ├── attendance.js         # 考勤状态
│   │   ├── salary.js             # 薪资状态
│   │   ├── organization.js       # 组织状态
│   │   └── permissions.js        # 权限状态
│   ├── views/
│   │   ├── Login.vue             # 登录页
│   │   ├── Dashboard.vue         # 仪表盘
│   │   ├── Employees.vue         # 员工管理
│   │   ├── Attendance.vue        # 考勤管理
│   │   ├── Salary.vue            # 薪资管理
│   │   ├── Organization.vue      # 组织配置
│   │   ├── Permissions.vue       # 权限管理
│   │   └── ImportExport.vue      # 导入导出
│   ├── components/
│   │   ├── AppHeader.vue         # 顶部导航
│   │   ├── AppSidebar.vue        # 侧边栏
│   │   ├── DataTable.vue         # 通用表格组件
│   │   ├── DataForm.vue          # 通用表单组件
│   │   ├── ConfirmDialog.vue     # 确认对话框
│   │   ├── FileUpload.vue        # 文件上传组件
│   │   └── TreeView.vue          # 树形组件
│   ├── composables/
│   │   ├── useApi.js             # API 请求封装
│   │   ├── useAuth.js            # 认证逻辑
│   │   ├── usePagination.js      # 分页逻辑
│   │   └── useValidation.js      # 表单验证
│   └── utils/
│       ├── validators.js         # 验证函数
│       └── formatters.js         # 格式化工具
```

### 核心模块重构示例

#### 1. API 层

现有 [core/api.js](file:///home/alex/hr-system/src/main/resources/static/core/api.js) 中的 API 定义将拆分为独立模块：

```javascript
// src/api/employees.js
import { apiClient } from './index'

export const employeesApi = {
  list: () => apiClient.get('/employees'),
  create: (data) => apiClient.post('/employees', data),
  update: (id, data) => apiClient.put(`/employees/${id}`, data),
  delete: (id) => apiClient.delete(`/employees/${id}`),
  resign: (data) => apiClient.post('/employees/resign', data),
  rehire: (data) => apiClient.post('/employees/rehire', data),
  uploadAvatar: (id, file) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient.post(`/employees/${id}/avatar`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
```

#### 2. 状态管理

使用 Pinia 替代全局 `initCache`：

```javascript
// src/stores/employees.js
import { defineStore } from 'pinia'
import { employeesApi } from '@/api/employees'

export const useEmployeeStore = defineStore('employees', {
  state: () => ({
    list: [],
    loading: false
  }),
  actions: {
    async fetchList() {
      this.loading = true
      try {
        this.list = await employeesApi.list()
      } finally {
        this.loading = false
      }
    },
    async create(data) {
      await employeesApi.create(data)
      await this.fetchList()
    },
    async update(id, data) {
      await employeesApi.update(id, data)
      await this.fetchList()
    },
    async delete(id) {
      await employeesApi.delete(id)
      await this.fetchList()
    }
  }
})
```

#### 3. 视图组件

现有 [modules/employees.js](file:///home/alex/hr-system/src/main/resources/static/modules/employees.js) 中的员工管理逻辑将重构为 Vue 组件：

```vue
<!-- src/views/Employees.vue -->
<script setup>
import { ref, onMounted } from 'vue'
import { useEmployeeStore } from '@/stores/employees'
import DataTable from '@/components/DataTable.vue'
import DataForm from '@/components/DataForm.vue'

const store = useEmployeeStore()
const showForm = ref(false)
const editingEmployee = ref(null)

const columns = [
  { key: 'employeeNo', label: '工号' },
  { key: 'name', label: '姓名' },
  { key: 'orgName', label: '部门' },
  { key: 'title', label: '岗位' },
  { key: 'status', label: '状态' }
]

const actions = [
  { label: '编辑', handler: (row) => { editingEmployee.value = row; showForm.value = true } },
  { label: (row) => row.status === '在职' ? '离职' : '复职', handler: toggleStatus },
  { label: '删除', handler: (row) => store.delete(row.id) }
]

onMounted(() => store.fetchList())
</script>

<template>
  <div class="employees-view">
    <button @click="showForm = true">新增员工</button>
    <DataTable 
      :data="store.list" 
      :columns="columns" 
      :actions="actions"
      :loading="store.loading"
    />
    <DataForm 
      v-if="showForm"
      :employee="editingEmployee"
      @submit="handleSave"
      @close="showForm = false"
    />
  </div>
</template>
```

---

## 四、迁移策略

### 阶段一：基础设施搭建

**优先级：高**

| 任务 | 内容 | 收益 | 验收指标 |
|------|------|------|----------|
| 初始化 Vue 3 项目 | 配置 Vite、Vue 3、Pinia、Vue Router | 建立新架构基础 | 项目可启动，基础路由可用 |
| 迁移 API 层 | 将 [core/api.js](file:///home/alex/hr-system/src/main/resources/static/core/api.js) 拆分为模块化 API | API 调用类型安全、可测试 | 所有 API 端点可正常调用 |
| 迁移认证模块 | 将 [core/auth.js](file:///home/alex/hr-system/src/main/resources/static/core/auth.js) 迁移至 Pinia store | 认证状态集中管理 | 登录、登出、自动登录功能正常 |

### 阶段二：核心业务模块迁移

**优先级：高**

| 任务 | 内容 | 收益 | 验收指标 |
|------|------|------|----------|
| 迁移仪表盘 | 将 [modules/dashboard.js](file:///home/alex/hr-system/src/main/resources/static/modules/dashboard.js) 重构为 Vue 组件 | 图表响应式更新 | 所有图表正常渲染，数据实时更新 |
| 迁移员工管理 | 将 [modules/employees.js](file:///home/alex/hr-system/src/main/resources/static/modules/employees.js) 重构为 Vue 组件 | 员工 CRUD 操作简化 | 员工增删改查、头像上传功能正常 |
| 迁移考勤管理 | 将 [modules/attendance.js](file:///home/alex/hr-system/src/main/resources/static/modules/attendance.js) 重构为 Vue 组件 | 考勤记录管理简化 | 考勤打卡、规则设置功能正常 |
| 迁移薪资管理 | 将 [modules/salary.js](file:///home/alex/hr-system/src/main/resources/static/modules/salary.js) 重构为 Vue 组件 | 薪资记录管理简化 | 薪资增删改查功能正常 |

### 阶段三：辅助模块迁移

**优先级：中**

| 任务 | 内容 | 收益 | 验收指标 |
|------|------|------|----------|
| 迁移组织配置 | 将 [modules/org.js](file:///home/alex/hr-system/src/main/resources/static/modules/org.js) 重构为 Vue 组件 | 组织架构管理简化 | 部门、岗位、职级管理功能正常 |
| 迁移权限管理 | 将 [modules/permissions.js](file:///home/alex/hr-system/src/main/resources/static/modules/permissions.js) 重构为 Vue 组件 | 权限配置简化 | 角色和权限管理功能正常 |
| 迁移导入导出 | 将 [modules/import-export.js](file:///home/alex/hr-system/src/main/resources/static/modules/import-export.js) 重构为 Vue 组件 | 数据导入导出简化 | 导入导出功能正常 |

### 阶段四：优化与清理

**优先级：低**

| 任务 | 内容 | 收益 | 验收指标 |
|------|------|------|----------|
| 组件库封装 | 提取通用组件（表格、表单、对话框） | 代码复用率提升 | 通用组件覆盖 80% 以上场景 |
| TypeScript 迁移 | 为关键模块添加类型定义 | 类型安全、开发体验提升 | 核心模块通过类型检查 |
| 旧代码清理 | 删除原有 JavaScript 文件 | 减少维护负担 | 旧文件全部移除，构建无警告 |

---

## 五、关键技术决策

### 1. 是否使用 UI 组件库

**建议**：暂不引入 Element Plus 等重量级组件库，保持现有样式风格。仅封装通用组件（表格、表单、对话框），确保与现有设计系统一致。

### 2. 路由方案

**建议**：使用 Vue Router 的 hash 模式，保持与现有 tab 切换逻辑兼容。路由路径映射为 `/dashboard`、`/employees` 等。

### 3. 构建输出

**建议**：Vite 构建输出至 `src/main/resources/static/dist/`，通过 Spring Boot 静态资源映射提供服务。开发时使用 Vite dev server 代理 API 请求。

### 4. 渐进式迁移

**建议**：采用渐进式迁移策略，新旧代码可共存。通过 iframe 或 Web Components 方式逐步替换现有模块，降低迁移风险。

---

## 六、风险评估

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 学习成本 | 团队成员需学习 Vue 3 | 提供培训文档和示例代码 |
| 迁移周期 | 业务模块较多，迁移耗时 | 分阶段迁移，优先核心模块 |
| 样式兼容 | 新组件与现有样式冲突 | 保持现有 CSS 变量和样式规范 |
| 性能影响 | Vue 3 运行时增加包体积 | 按需加载，代码分割，gzip 压缩 |

---

## 七、后续规划

1. **引入 TypeScript**：为 API 层和状态管理添加完整类型定义
2. **单元测试**：使用 Vitest 为核心逻辑编写测试用例
3. **E2E 测试**：使用 Playwright 覆盖关键业务流程
4. **性能优化**：路由懒加载、组件异步加载、虚拟滚动
5. **国际化**：支持多语言切换
