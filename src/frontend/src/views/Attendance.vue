<template>
  <div>
    <div class="panel">
      <div class="panel-header">
        <h2>考勤管理</h2>
        <p>手动打卡、规则设置、人脸打卡</p>
      </div>
      <div class="panel-actions">
        <button @click="showManualForm = true">手动打卡</button>
        <button @click="showRulesModal = true">规则设置</button>
        <button @click="showFaceModal = true">人脸打卡</button>
      </div>
    </div>
    <div class="table-panel">
      <table>
        <thead>
          <tr>
            <th :class="getClass('employee')" @click="toggle('employee')">员工</th>
            <th :class="getClass('workDate')" @click="toggle('workDate')">日期</th>
            <th :class="getClass('checkIn')" @click="toggle('checkIn')">签到</th>
            <th :class="getClass('checkOut')" @click="toggle('checkOut')">签退</th>
            <th :class="getClass('status')" @click="toggle('status')">状态</th>
            <th>详情</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="store.loading"><td colspan="7" style="text-align:center;color:var(--muted)">加载中...</td></tr>
          <template v-else>
          <tr v-for="row in sorted" :key="row.id">
            <td>{{ row.employee?.name || row.employee?.employeeNo || '-' }}</td>
            <td>{{ row.workDate }}</td>
            <td>{{ row.checkIn || '-' }}</td>
            <td>{{ row.checkOut || '-' }}</td>
            <td><span :class="getStatusClass(row.status)">{{ row.status }}</span></td>
            <td>{{ getStatusDetail(row) }}</td>
            <td>
              <button class="ghost" @click="editRecord(row)">编辑</button>
              <button class="ghost" @click="confirmDelete(row.id)">删除</button>
            </td>
          </tr>
          </template>
        </tbody>
      </table>
    </div>

    <ModalDialog v-if="showManualForm" title="手动打卡" submitText="打卡"
      @submit="handleManualCheckin" @close="showManualForm = false">
      <form @submit.prevent>
        <label>员工
          <select v-model="manualForm.employeeId" required>
            <option value="">请选择在职员工</option>
            <option v-for="emp in employeeStore.activeEmployees" :key="emp.id" :value="emp.id">
              {{ emp.employeeNo }} - {{ emp.name }}
            </option>
          </select>
        </label>
        <label>备注 <input v-model="manualForm.note" placeholder="备注（可选）"></label>
      </form>
    </ModalDialog>

    <ModalDialog v-if="showRulesModal" title="考勤规则设置" @submit="handleSaveRules" @close="showRulesModal = false">
      <form @submit.prevent>
        <h4>标准工时设置</h4>
        <div class="grid-2">
          <label>上班时间 <input v-model="ruleForm.workStartTime" type="time"></label>
          <label>下班时间 <input v-model="ruleForm.workEndTime" type="time"></label>
        </div>
        <h4>考勤规则设置</h4>
        <div class="grid-2">
          <label>迟到宽限(分钟) <input v-model.number="ruleForm.lateGraceMinutes" type="number"></label>
          <label>旷工阈值(分钟) <input v-model.number="ruleForm.absentThresholdMinutes" type="number"></label>
        </div>
        <div class="grid-2">
          <label>早退宽限(分钟) <input v-model.number="ruleForm.earlyLeaveGraceMinutes" type="number"></label>
        </div>
      </form>
    </ModalDialog>

    <ModalDialog v-if="showFaceModal" title="人脸打卡" submitText="打卡"
      @submit="handleFaceAttendance" @close="showFaceModal = false">
      <form @submit.prevent>
        <label>选择员工
          <select v-model="faceForm.employeeId" required>
            <option value="">请选择</option>
            <option v-for="emp in employeeStore.activeEmployees" :key="emp.id" :value="emp.id">
              {{ emp.employeeNo }} - {{ emp.name }}
            </option>
          </select>
        </label>
        <label>识别文件 <input ref="faceFileInput" type="file" accept="image/*" required></label>
      </form>
    </ModalDialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAttendanceStore } from '@/stores/attendance'
import { useEmployeeStore } from '@/stores/employees'
import { useSortable } from '@/composables/useSortable'
import ModalDialog from '@/components/ModalDialog.vue'

const store = useAttendanceStore()
const employeeStore = useEmployeeStore()

const showManualForm = ref(false)
const showRulesModal = ref(false)
const showFaceModal = ref(false)
const faceFileInput = ref(null)

const manualForm = reactive({ employeeId: '', note: '' })
const ruleForm = reactive({ workStartTime: '09:00', workEndTime: '18:00', lateGraceMinutes: 10, absentThresholdMinutes: 240, earlyLeaveGraceMinutes: 10 })
const faceForm = reactive({ employeeId: '' })

const listRef = computed(() => store.list)
const { sorted, toggle, getClass } = useSortable(listRef, 'employee.name', 'desc')

function getStatusClass(status) {
  if (!status) return ''
  if (status.includes('旷工')) return 'status-absent'
  if (status.includes('迟到') || status.includes('早退')) return 'status-warning'
  return 'status-normal'
}

function getStatusDetail(row) {
  const parts = []
  if (row.lateMinutes) parts.push(`迟到${row.lateMinutes}分钟`)
  if (row.absentMinutes) parts.push(`旷工${row.absentMinutes}分钟`)
  if (row.earlyLeaveMinutes) parts.push(`早退${row.earlyLeaveMinutes}分钟`)
  return parts.length > 0 ? parts.join(', ') : '-'
}

function editRecord(row) {
  manualForm.employeeId = row.employee?.id
  manualForm.note = row.note || ''
  showManualForm.value = true
}

function confirmDelete(id) {
  if (confirm('确定要删除该考勤记录吗？')) {
    store.delete(id)
  }
}

async function handleManualCheckin() {
  try {
    const data = {
      employeeId: Number(manualForm.employeeId),
      note: manualForm.note || null
    }
    await store.create(data)
    alert('打卡成功')
    showManualForm.value = false
    manualForm.employeeId = ''
    manualForm.note = ''
  } catch (e) {
    alert('打卡失败：' + (e.message || '未知错误'))
  }
}

async function handleSaveRules() {
  await store.updateRule(ruleForm)
  showRulesModal.value = false
}

async function handleFaceAttendance() {
  const file = faceFileInput.value?.files[0]
  if (!faceForm.employeeId || !file) {
    alert('请选择员工和识别文件')
    return
  }
  const data = await store.faceAttendance(faceForm.employeeId, file)
  alert(`打卡成功：${data.workDate} ${data.checkIn || data.checkOut || ''}`)
  showFaceModal.value = false
  faceForm.employeeId = ''
  await store.fetchList()
}

onMounted(async () => {
  await Promise.all([store.fetchList(), store.fetchRule(), employeeStore.fetchList()])
  if (store.rule) {
    ruleForm.workStartTime = store.rule.workStartTime || '09:00'
    ruleForm.workEndTime = store.rule.workEndTime || '18:00'
    ruleForm.lateGraceMinutes = store.rule.lateGraceMinutes ?? 10
    ruleForm.absentThresholdMinutes = store.rule.absentThresholdMinutes ?? 240
  }
})
</script>
