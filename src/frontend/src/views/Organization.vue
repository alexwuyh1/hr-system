<template>
  <div>
    <div class="panel">
      <div class="panel-header">
        <h2>组织配置</h2>
        <p>维护岗位、部门与职级</p>
      </div>
    </div>
    <div class="org-layout">
      <div class="org-sidebar">
        <h3>类型</h3>
        <ul>
          <li v-for="t in orgTypes" :key="t.key" :class="{ active: store.activeType === t.key }"
            @click="store.setActiveType(t.key)">{{ t.label }}</li>
        </ul>
        <button @click="showForm = true">新增</button>
      </div>
      <div class="org-main">
        <div class="org-header">
          <h3>{{ currentConfig.title }}</h3>
        </div>
        <div v-if="currentConfig.showTree" class="org-tree">
          <ul class="tree">
            <OrganizationTreeNode v-for="node in store.positionTree" :key="node.id" :node="node" />
          </ul>
        </div>
        <table>
          <thead>
            <tr>
              <th v-for="col in currentColumns" :key="col.key">{{ col.label }}</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="store.loading"><td :colspan="currentColumns.length + 1" style="text-align:center;color:var(--muted)">加载中...</td></tr>
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
      </div>
    </div>

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
          <label>所属部门
            <select v-model="formData.parentId" required>
              <option value="">请选择</option>
              <option v-for="dept in store.deptOptions" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
            </select>
          </label>
          <label>职级
            <select v-model="formData.gradeId" required>
              <option value="">请选择</option>
              <option v-for="grade in store.gradeOptions" :key="grade.id" :value="grade.id">{{ grade.name }}</option>
            </select>
          </label>
        </template>
        <template v-else>
          <label>名称 <input v-model="formData.name" required placeholder="输入职级名称"></label>
          <label>等级 <input v-model.number="formData.level" type="number" required placeholder="职级等级数值"></label>
        </template>
      </form>
    </ModalDialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, defineAsyncComponent } from 'vue'
import { useOrganizationStore } from '@/stores/organization'
import ModalDialog from '@/components/ModalDialog.vue'

const OrganizationTreeNode = defineAsyncComponent(() => import('@/components/OrganizationTreeNode.vue'))

const store = useOrganizationStore()
const showForm = ref(false)
const formData = reactive({ name: '', parentId: '', gradeId: '', level: '' })

const orgTypes = [
  { key: 'position', label: '岗位' },
  { key: 'dept', label: '部门' },
  { key: 'grade', label: '职级' }
]

const currentConfig = computed(() => {
  const config = {
    position: { title: '岗位管理', type: '岗位', showTree: true },
    dept: { title: '部门管理', type: '部门', showTree: false },
    grade: { title: '职级管理', type: '职级', showTree: false }
  }
  return config[store.activeType]
})

const currentColumns = computed(() => {
  const heads = {
    position: [
      { key: 'name', label: '名称' },
      { key: 'parentName', label: '部门', render: (_, row) => row.parent ? row.parent.name : '-' },
      { key: 'gradeName', label: '职级', render: (_, row) => row.grade ? row.grade.name : '-' }
    ],
    dept: [
      { key: 'name', label: '名称' },
      { key: 'parentName', label: '上级', render: (_, row) => row.parent ? row.parent.name : '-' }
    ],
    grade: [
      { key: 'name', label: '名称' },
      { key: 'level', label: '等级' }
    ]
  }
  return heads[store.activeType]
})

function deleteItem(item) {
  if (confirm(`确定要删除${currentConfig.value.type}"${item.name}"吗？`)) {
    store.delete(item.id)
  }
}

async function handleCreate() {
  if (!formData.name.trim()) {
    alert('请输入名称')
    return
  }
  const data = { name: formData.name.trim(), type: currentConfig.value.type }
  if (store.activeType === 'dept') {
    data.parentId = formData.parentId ? Number(formData.parentId) : null
    data.level = null
  } else if (store.activeType === 'position') {
    data.parentId = Number(formData.parentId)
    data.gradeId = Number(formData.gradeId)
    data.level = null
  } else {
    data.parentId = null
    data.level = Number(formData.level)
  }
  await store.create(data)
  showForm.value = false
  Object.assign(formData, { name: '', parentId: '', gradeId: '', level: '' })
}

onMounted(() => store.init())
</script>
