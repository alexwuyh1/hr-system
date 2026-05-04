<template>
  <div>
    <div class="panel">
      <div class="panel-header">
        <h2>数据导入导出</h2>
        <p>支持 CSV / Excel 批量导入导出</p>
      </div>
      <div class="form-actions">
        <button @click="showImportModal = true">数据导入</button>
        <button class="ghost" @click="showExportModal = true">数据导出</button>
      </div>
      <p v-if="resultMsg" class="subtitle">{{ resultMsg }}</p>
    </div>

    <ModalDialog v-if="showImportModal" title="数据导入" submitText="导入"
      @submit="handleImport" @close="showImportModal = false">
      <form @submit.prevent>
        <label>数据类型
          <select v-model="importType" required>
            <option value="">请选择数据类型</option>
            <option value="employees">员工数据</option>
            <option value="attendance">考勤数据</option>
          </select>
        </label>
        <label>选择文件 <input ref="importFileInput" type="file" accept=".csv,.xlsx" required></label>
      </form>
    </ModalDialog>

    <ModalDialog v-if="showExportModal" title="数据导出" submitText="导出"
      @submit="handleExport" @close="showExportModal = false">
      <form @submit.prevent>
        <label>数据类型
          <select v-model="exportType" required>
            <option value="">请选择</option>
            <option value="employees">员工数据</option>
            <option value="attendance">考勤数据</option>
            <option value="salary">薪资数据</option>
          </select>
        </label>
        <label>导出格式
          <select v-model="exportFormat" required>
            <option value="csv">CSV</option>
            <option value="xlsx">Excel</option>
          </select>
        </label>
      </form>
    </ModalDialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { importExportApi } from '@/api/dashboard'
import ModalDialog from '@/components/ModalDialog.vue'

const showImportModal = ref(false)
const showExportModal = ref(false)
const importType = ref('')
const importFileInput = ref(null)
const exportType = ref('')
const exportFormat = ref('csv')
const resultMsg = ref('')

async function handleImport() {
  const file = importFileInput.value?.files[0]
  if (!importType.value || !file) {
    alert('请选择数据类型和文件')
    return
  }
  try {
    const result = await importExportApi.import(importType.value, file)
    resultMsg.value = `导入成功：${result.imported} 条记录`
    showImportModal.value = false
    importType.value = ''
  } catch {
    showImportModal.value = false
  }
}

async function handleExport() {
  if (!exportType.value) {
    alert('请选择数据类型')
    return
  }
  try {
    const blob = await importExportApi.export(exportType.value, exportFormat.value)
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${exportType.value}.${exportFormat.value === 'xlsx' ? 'xlsx' : 'csv'}`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    showExportModal.value = false
    resultMsg.value = '导出成功'
  } catch {
    showExportModal.value = false
  }
}
</script>
