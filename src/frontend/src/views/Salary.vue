<template>
  <div>
    <div class="panel">
      <div class="panel-header">
        <h2>薪资记录</h2>
        <p>按月管理员工薪资</p>
      </div>
      <div class="panel-actions">
        <button @click="showForm = true; editingRecord = null">新增薪资</button>
      </div>
    </div>
    <div class="table-panel">
      <table>
        <thead>
          <tr>
            <th :class="getClass('employee')" @click="toggle('employee')">员工</th>
            <th :class="getClass('salaryMonth')" @click="toggle('salaryMonth')">月份</th>
            <th :class="getClass('baseSalary')" @click="toggle('baseSalary')">基本</th>
            <th :class="getClass('bonus')" @click="toggle('bonus')">奖金</th>
            <th :class="getClass('deduction')" @click="toggle('deduction')">扣款</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="store.loading"><td colspan="6" style="text-align:center;color:var(--muted)">加载中...</td></tr>
          <template v-else>
          <tr v-for="row in sorted" :key="row.id">
            <td>{{ row.employee?.name || row.employee?.employeeNo || '-' }}</td>
            <td>{{ row.salaryMonth }}</td>
            <td>{{ row.baseSalary }}</td>
            <td>{{ row.bonus }}</td>
            <td>{{ row.deduction }}</td>
            <td>
              <button class="ghost" @click="editRecord(row)">编辑</button>
              <button class="ghost" @click="confirmDelete(row.id)">删除</button>
            </td>
          </tr>
          </template>
        </tbody>
      </table>
    </div>

    <ModalDialog v-if="showForm" :title="editingRecord ? '编辑薪资记录' : '新增薪资记录'"
      @submit="handleSave" @close="showForm = false">
      <form @submit.prevent>
        <label>员工
          <select v-model="formData.employeeId" required>
            <option value="">请选择员工</option>
            <option v-for="emp in employeeStore.list" :key="emp.id" :value="emp.id">
              {{ emp.employeeNo }} - {{ emp.name }}
            </option>
          </select>
        </label>
        <label>薪资月份 <input v-model="formData.salaryMonth" type="month" required></label>
        <div class="grid-2">
          <label>基本薪资 <input v-model.number="formData.baseSalary" type="number" step="0.01" placeholder="≥ 0" required></label>
          <label>奖金 <input v-model.number="formData.bonus" type="number" step="0.01" placeholder="≥ 0" required></label>
        </div>
        <label>扣款 <input v-model.number="formData.deduction" type="number" step="0.01" placeholder="≥ 0" required></label>
      </form>
    </ModalDialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useSalaryStore } from '@/stores/salary'
import { useEmployeeStore } from '@/stores/employees'
import { useSortable } from '@/composables/useSortable'
import ModalDialog from '@/components/ModalDialog.vue'

const store = useSalaryStore()
const employeeStore = useEmployeeStore()
const showForm = ref(false)
const editingRecord = ref(null)

const formData = reactive({ employeeId: '', salaryMonth: '', baseSalary: '', bonus: '', deduction: '' })

const listRef = computed(() => store.list)
const { sorted, toggle, getClass } = useSortable(listRef, 'employee.name', 'desc')

function editRecord(row) {
  editingRecord.value = row
  Object.assign(formData, {
    employeeId: row.employee?.id,
    salaryMonth: row.salaryMonth,
    baseSalary: row.baseSalary,
    bonus: row.bonus,
    deduction: row.deduction
  })
  showForm.value = true
}

function confirmDelete(id) {
  if (confirm('确定要删除该薪资记录吗？')) {
    store.delete(id)
  }
}

async function handleSave() {
  const data = {
    employeeId: Number(formData.employeeId),
    salaryMonth: formData.salaryMonth,
    baseSalary: Number(formData.baseSalary),
    bonus: Number(formData.bonus),
    deduction: Number(formData.deduction)
  }
  if (data.baseSalary < 0 || data.bonus < 0 || data.deduction < 0) {
    alert('金额不能小于 0')
    return
  }
  if (editingRecord.value) {
    await store.update(editingRecord.value.id, data)
  } else {
    await store.create(data)
  }
  showForm.value = false
  editingRecord.value = null
  Object.assign(formData, { employeeId: '', salaryMonth: '', baseSalary: '', bonus: '', deduction: '' })
}

onMounted(async () => {
  await Promise.all([store.fetchList(), employeeStore.fetchList()])
})
</script>
