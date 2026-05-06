<template>
  <div>
    <div class="panel">
      <div class="panel-header">
        <h2>组织配置</h2>
        <p>维护岗位、部门与职级</p>
      </div>
      <div class="org-tree">
        <ul class="tree">
          <OrganizationTreeNode v-for="node in store.positionTree" :key="node.id" :node="node" />
        </ul>
      </div>
    </div>

    <MasterDetailLayout>
      <template #sidebar-top>
        <h3>类型</h3>
      </template>
      <template #sidebar-list>
        <li v-for="t in orgTypes" :key="t.key"
          :class="{ active: store.activeType === t.key }"
          @click="store.setActiveType(t.key)">
          {{ t.label }}
        </li>
      </template>
      <template #sidebar-actions>
        <button @click="showForm = true">新增</button>
      </template>

      <template #header>
        <h3>{{ currentConfig.title }}</h3>
      </template>
      <template #content>
        <table>
            <thead>
              <tr>
                <th v-for="col in currentColumns" :key="col.key">{{ col.label }}</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="store.loading">
                <td :colspan="currentColumns.length + 1" style="text-align:center;color:var(--muted)">加载中...</td>
              </tr>
              <template v-else>
                <tr v-for="item in store.filteredItems" :key="item.id">
                  <td v-for="col in currentColumns" :key="col.key">
                    <template v-if="col.render">{{ col.render(null, item) }}</template>
                    <template v-else>{{ item[col.key] }}</template>
                  </td>
                  <td>
                    <button class="ghost" @click="deleteItem(item)">删除</button>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
      </template>
    </MasterDetailLayout>

    <ModalDialog v-if="showForm" :title="'新增' + currentConfig.type" @submit="handleCreate" @close="showForm = false">
      <form @submit.prevent>
        <template v-if="store.activeType === 'dept'">
          <label>名称 <input v-model="formData.name" required placeholder="输入部门名称"></label>
          <label>上级部门
            <select v-model="formData.parentId">
              <option value="">无</option>
              <option v-for="dept in store.deptOptions" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
            </select>
          </label>
        </template>
        <template v-else-if="store.activeType === 'position'">
          <label>名称 <input v-model="formData.name" required placeholder="输入岗位名称"></label>
          <label>部门
            <select v-model="formData.departmentId" required>
              <option value="">选择部门</option>
              <option v-for="dept in store.deptOptions" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
            </select>
          </label>
          <label>职级
            <select v-model="formData.gradeId">
              <option value="">无</option>
              <option v-for="g in store.gradeOptions" :key="g.id" :value="g.id">{{ g.name }}</option>
            </select>
          </label>
        </template>
        <template v-else>
          <label>名称 <input v-model="formData.name" required placeholder="输入职级名称"></label>
        </template>
        <div class="form-actions">
          <button type="submit">保存</button>
        </div>
      </form>
    </ModalDialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useOrganizationStore } from '@/stores/organization'
import MasterDetailLayout from '@/layouts/MasterDetail.vue'
import OrganizationTreeNode from '@/components/OrganizationTreeNode.vue'
import ModalDialog from '@/components/ModalDialog.vue'

const store = useOrganizationStore()
const showForm = ref(false)
const formData = reactive({ name: '', parentId: '', departmentId: '', gradeId: '' })

const orgTypes = [
  { key: 'dept', label: '部门' },
  { key: 'position', label: '岗位' },
  { key: 'grade', label: '职级' }
]

const currentConfig = computed(() => {
  switch (store.activeType) {
    case 'dept': return { title: '部门管理', type: '部门', showTree: true }
    case 'position': return { title: '岗位管理', type: '岗位', showTree: true }
    case 'grade': return { title: '职级管理', type: '职级', showTree: false }
    default: return { title: '', type: '', showTree: false }
  }
})

const currentColumns = computed(() => {
  switch (store.activeType) {
    case 'dept': return [{ key: 'name', label: '名称' }, { key: 'parentName', label: '上级部门' }]
    case 'position': return [{ key: 'name', label: '名称' }, { key: 'departmentName', label: '部门' }, { key: 'gradeName', label: '职级' }]
    case 'grade': return [{ key: 'name', label: '名称' }]
    default: return []
  }
})

function resetForm() {
  Object.assign(formData, { name: '', parentId: '', departmentId: '', gradeId: '' })
}

async function handleCreate() {
  const data = { name: formData.name }
  if (store.activeType === 'dept' && formData.parentId) data.parentId = formData.parentId
  if (store.activeType === 'position') {
    data.departmentId = formData.departmentId
    if (formData.gradeId) data.gradeId = formData.gradeId
  }
  await store.createItem(data)
  showForm.value = false
  resetForm()
}

async function deleteItem(item) {
  if (confirm(`确认删除 "${item.name}"？`)) {
    await store.deleteItem(item.id)
  }
}

onMounted(() => {
  store.init()
})
</script>
