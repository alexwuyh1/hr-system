<template>
  <div>
    <div class="panel">
      <div class="panel-header">
        <h2>员工中心</h2>
        <p>员工管理与头像上传</p>
      </div>
      <div class="panel-actions">
        <button @click="showForm = true; editingEmployee = null">新增员工</button>
      </div>
    </div>
    <div v-if="employeeStore.loading" class="subtitle" style="text-align:center;padding:40px">加载中...</div>
    <div v-else class="employee-cards-grid">
      <div v-for="emp in employeeStore.list" :key="emp.id" class="employee-card">
        <div class="employee-card-avatar">
          <img :src="emp.avatarUrl || ''" :alt="emp.name" />
        </div>
        <div class="employee-card-info">
          <div class="employee-card-name">{{ emp.name }} <span class="employee-card-no">({{ emp.employeeNo }})</span></div>
          <div>部门：{{ emp.orgName || '-' }}</div>
          <div>岗位：{{ emp.positionName || '-' }}</div>
          <div>邮箱：{{ emp.email || '-' }}</div>
          <div>电话：{{ emp.phone || '-' }}</div>
          <div class="employee-card-status">
            <span :style="{ color: emp.status === '在职' ? '#16a34a' : '#dc2626', fontWeight: 600 }">{{ emp.status }}</span>
          </div>
        </div>
        <div class="employee-card-actions">
          <button class="ghost" @click="openEdit(emp)">编辑</button>
          <button class="ghost" @click="toggleStatus(emp)">{{ emp.status === '在职' ? '离职' : '复职' }}</button>
          <button class="ghost" @click="confirmDelete(emp.id)">删除</button>
          <button class="ghost" @click="openAvatarUpload(emp.id)">上传头像</button>
        </div>
      </div>
    </div>

    <ModalDialog v-if="showForm" :title="editingEmployee ? '编辑员工' : '新增员工'"
      :submitText="editingEmployee ? '保存' : '入职'" @submit="handleSave" @close="showForm = false">
      <form @submit.prevent>
        <div class="grid-2">
          <label>工号 <input v-model="formData.employeeNo" required placeholder="唯一标识，如 EMP001"></label>
          <label>姓名 <input v-model="formData.name" required placeholder="员工真实姓名"></label>
        </div>
        <div class="grid-2">
          <label>岗位
            <select v-model="formData.positionId" required>
              <option value="">请选择岗位</option>
              <option v-for="pos in positionOptions" :key="pos.id" :value="pos.id">{{ pos.name }}</option>
            </select>
          </label>
          <label>入职日期 <input v-model="formData.hireDate" type="date" required></label>
        </div>
        <div class="grid-2">
          <label>邮箱 <input v-model="formData.email" type="email" placeholder="example@company.com"></label>
          <label>电话 <input v-model="formData.phone" placeholder="11 位手机号，如 13800138000"></label>
        </div>
        <label>直属上级
          <select v-model="formData.managerId">
            <option value="">无</option>
            <option v-for="emp in managerOptions" :key="emp.id" :value="emp.id">{{ emp.employeeNo }} - {{ emp.name }}</option>
          </select>
        </label>
      </form>
    </ModalDialog>

    <ModalDialog v-if="showAvatarModal" title="上传头像" submitText="上传"
      @submit="handleAvatarUpload" @close="showAvatarModal = false">
      <form @submit.prevent>
        <label>头像文件 <input ref="avatarFileInput" type="file" accept="image/*" required></label>
      </form>
    </ModalDialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useEmployeeStore } from '@/stores/employees'
import { useOrganizationStore } from '@/stores/organization'
import ModalDialog from '@/components/ModalDialog.vue'

const employeeStore = useEmployeeStore()
const orgStore = useOrganizationStore()
const showForm = ref(false)
const showAvatarModal = ref(false)
const editingEmployee = ref(null)
const avatarFileInput = ref(null)
const uploadingEmployeeId = ref(null)

const formData = reactive({ employeeNo: '', name: '', positionId: '', hireDate: '', email: '', phone: '', managerId: '' })

const positionOptions = computed(() => orgStore.list.filter(o => o.type === '岗位'))
const managerOptions = computed(() => employeeStore.list.filter(e => e.status === '在职' && e.id !== editingEmployee.value?.id))

function openEdit(emp) {
  editingEmployee.value = emp
  Object.assign(formData, {
    employeeNo: emp.employeeNo, name: emp.name, positionId: emp.positionId,
    hireDate: emp.hireDate, email: emp.email, phone: emp.phone, managerId: emp.managerId
  })
  showForm.value = true
}

function toggleStatus(emp) {
  const isActive = emp.status === '在职'
  employeeStore.toggleStatus(emp.employeeNo, isActive)
}

function confirmDelete(id) {
  if (confirm('确定要删除该员工吗？此操作不可恢复。')) {
    employeeStore.delete(id)
  }
}

function openAvatarUpload(id) {
  uploadingEmployeeId.value = id
  showAvatarModal.value = true
}

async function handleSave() {
  const data = {
    ...formData,
    positionId: Number(formData.positionId),
    managerId: formData.managerId ? Number(formData.managerId) : null,
    status: '在职'
  }
  if (editingEmployee.value) {
    await employeeStore.update(editingEmployee.value.id, data)
  } else {
    await employeeStore.create(data)
  }
  showForm.value = false
  editingEmployee.value = null
  Object.assign(formData, { employeeNo: '', name: '', positionId: '', hireDate: '', email: '', phone: '', managerId: '' })
}

async function handleAvatarUpload() {
  const file = avatarFileInput.value?.files[0]
  if (!file) { alert('请选择头像文件'); return }
  await employeeStore.uploadAvatar(uploadingEmployeeId.value, file)
  showAvatarModal.value = false
}

onMounted(async () => {
  await Promise.all([employeeStore.fetchList(), orgStore.init()])
})
</script>
