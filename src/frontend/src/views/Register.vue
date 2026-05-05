<template>
  <div>
    <div class="panel">
      <div class="panel-header">
        <h2>注册账号</h2>
        <p>为在职员工创建系统账号，用户名和密码根据工号自动生成</p>
      </div>
    </div>

    <div class="panel">
      <form @submit.prevent="handleRegister">
        <div class="grid-2 form-fields">
          <div class="stat">
            <label for="reg-employee">选择员工</label>
            <select id="reg-employee" v-model="form.employeeId" required>
              <option value="">请选择在职员工</option>
              <option v-for="emp in activeEmployees" :key="emp.id" :value="emp.id">
                {{ emp.employeeNo }} — {{ emp.name }} ({{ emp.orgName || '未分配部门' }})
              </option>
            </select>
          </div>
          <div class="stat">
            <label for="reg-role">分配角色</label>
            <select id="reg-role" v-model="form.role" required>
              <option value="">请选择角色</option>
              <option v-for="r in roles" :key="r.role" :value="r.role">
                {{ r.role }}（{{ r.roleMode === 'blacklist' ? '黑名单' : '白名单' }}）
              </option>
            </select>
          </div>
        </div>
        <div class="form-actions">
          <button type="submit" :disabled="loading">注册账号</button>
        </div>
        <p class="hint">用户名格式：hr{工号}，密码格式：hr{工号}!</p>
      </form>

      <div v-if="result" class="stat" style="margin-top:20px;background:#e6ffed;">
        <p style="margin:0 0 8px;font-weight:600;color:#006644">✅ 注册成功，请将以下凭证告知用户：</p>
        <p style="margin:2px 0"><strong>用户名：</strong>{{ result.username }}</p>
        <p style="margin:2px 0"><strong>密码：</strong>{{ result.password }}</p>
        <p style="margin:2px 0"><strong>角色：</strong>{{ result.role }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useEmployeeStore } from '@/stores/employees'
import { usePermissionStore } from '@/stores/permissions'
import { authApi } from '@/api/auth'

const employeeStore = useEmployeeStore()
const permissionStore = usePermissionStore()

const form = reactive({ employeeId: '', role: '' })
const result = ref(null)
const loading = ref(false)

const activeEmployees = computed(() => employeeStore.list.filter(e => e.status === '在职'))
const roles = computed(() => permissionStore.roles)

onMounted(async () => {
  await employeeStore.fetchList()
  await permissionStore.loadRoles()
})

async function handleRegister() {
  if (!form.employeeId || !form.role) {
    alert('请选择员工和角色')
    return
  }
  loading.value = true
  result.value = null
  try {
    result.value = await authApi.register({
      employeeId: parseInt(form.employeeId),
      role: form.role
    })
  } catch (e) {
    const msg = e.response?.data?.message || e.message || '注册失败'
    alert('注册失败: ' + msg)
  } finally {
    loading.value = false
  }
}
</script>
