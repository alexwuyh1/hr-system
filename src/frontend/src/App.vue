<template>
  <div class="app">
    <header class="hero">
      <div>
        <p class="kicker">HR SUITE</p>
        <h1>人事管理系统</h1>
        <p class="subtitle">员工、考勤、薪资与报表在一个清爽的工作台完成。</p>
      </div>
    </header>

    <template v-if="!authStore.isAuthenticated">
      <section class="panel" id="auth-panel">
        <div class="panel-header">
          <h2>登录</h2>
          <p>默认管理员：admin / admin123</p>
        </div>
        <form @submit.prevent="handleLogin" class="grid-2 form-fields">
          <div class="stat">
            <label for="login-username">用户名</label>
            <input id="login-username" v-model="loginForm.username" placeholder="输入用户名" required />
          </div>
          <div class="stat">
            <label for="login-password">密码</label>
            <input id="login-password" v-model="loginForm.password" type="password" placeholder="输入密码" required />
          </div>
        </form>
        <div class="form-actions">
          <button @click="handleLogin">登录</button>
        </div>
      </section>
    </template>

    <template v-else>
      <section class="workspace">
        <nav class="tabs">
          <button v-for="tab in tabs" :key="tab.key" :class="{ active: currentTab === tab.key }"
            @click="currentTab = tab.key">
            {{ tab.label }}
          </button>
          <span class="role-badge">{{ authStore.currentRole }}</span>
          <button class="ghost" @click="authStore.logout">登出</button>
        </nav>

        <DashboardView v-if="currentTab === 'dashboard'" />
        <EmployeesView v-if="currentTab === 'employees'" />
        <AttendanceView v-if="currentTab === 'attendance'" />
        <SalaryView v-if="currentTab === 'salary'" />
        <OrganizationView v-if="currentTab === 'org-config'" />
        <ImportExportView v-if="currentTab === 'import-export'" />
        <PermissionsView v-if="currentTab === 'permissions'" />
        <RegisterView v-if="currentTab === 'register'" />
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, defineAsyncComponent } from 'vue'
import { useAuthStore } from '@/stores/auth'
const DashboardView = defineAsyncComponent(() => import('@/views/Dashboard.vue'))
const EmployeesView = defineAsyncComponent(() => import('@/views/Employees.vue'))
const AttendanceView = defineAsyncComponent(() => import('@/views/Attendance.vue'))
const SalaryView = defineAsyncComponent(() => import('@/views/Salary.vue'))
const OrganizationView = defineAsyncComponent(() => import('@/views/Organization.vue'))
const ImportExportView = defineAsyncComponent(() => import('@/views/ImportExport.vue'))
const PermissionsView = defineAsyncComponent(() => import('@/views/Permissions.vue'))
const RegisterView = defineAsyncComponent(() => import('@/views/Register.vue'))

const authStore = useAuthStore()
const currentTab = ref('dashboard')
const loginForm = reactive({ username: '', password: '' })

const tabs = [
  { key: 'dashboard', label: '仪表盘' },
  { key: 'employees', label: '员工管理' },
  { key: 'attendance', label: '考勤管理' },
  { key: 'salary', label: '薪资管理' },
  { key: 'org-config', label: '组织配置' },
  { key: 'register', label: '注册账号' },
  { key: 'permissions', label: '权限管理' },
  { key: 'import-export', label: '导入导出' }
]

async function handleLogin() {
  await authStore.login(loginForm.username, loginForm.password)
  loginForm.username = ''
  loginForm.password = ''
  currentTab.value = 'dashboard'
}

onMounted(() => {
  authStore.init()
})
</script>
