<template>
  <div>
    <div class="panel">
      <div class="panel-header">
        <h2>权限管理</h2>
        <p>角色管理与权限规则配置</p>
      </div>
    </div>

    <MasterDetailLayout>
      <template #sidebar-top>
        <h3>角色列表</h3>
      </template>
      <template #sidebar-list>
        <li v-for="role in store.roles" :key="role.role"
          :class="{ active: store.selectedRole === role.role }"
          @click="store.selectRole(role.role, role.roleMode)">
          <span class="role-name">{{ role.role }}</span>
          <span v-if="store.selectedRole === role.role" class="mode-badge"
            :class="role.roleMode === 'blacklist' ? 'mode-badge-blacklist' : 'mode-badge-whitelist'">
            {{ role.roleMode === 'blacklist' ? '黑' : '白' }}
          </span>
        </li>
      </template>
      <template #sidebar-actions>
        <button @click="showRoleModal = true">新增角色</button>
        <button v-if="store.selectedRole" class="ghost" @click="confirmDeleteRole(store.selectedRole)">删除角色</button>
      </template>

      <template #header>
        <h3>{{ store.selectedRole || '请选择角色' }}</h3>
        <span v-if="store.selectedRoleMode" class="role-mode-tag"
          :class="store.selectedRoleMode === 'blacklist' ? 'tag-blacklist' : 'tag-whitelist'">
          {{ store.selectedRoleMode === 'blacklist' ? '黑名单模式' : '白名单模式' }}
        </span>
        <button v-if="store.selectedRole" @click="showPermModal = true">新增权限</button>
      </template>
      <template #content>
        <table v-if="store.selectedRole">
          <thead>
            <tr>
              <th>方法</th>
              <th>路径</th>
              <th>模式</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="perm in store.filteredPermissions" :key="perm.id">
              <td>{{ perm.method }}</td>
              <td>{{ perm.pathPrefix }}</td>
              <td>
                <span :class="store.selectedRoleMode === 'blacklist' ? 'mode-deny' : 'mode-allow'">
                  {{ store.selectedModeLabel }}
                </span>
              </td>
              <td>
                <button class="ghost" @click="store.deletePermission(perm.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else style="color:var(--muted);text-align:center;padding:40px">请选择一个角色</div>
      </template>
    </MasterDetailLayout>

    <ModalDialog v-if="showRoleModal" title="新增角色" @submit="handleCreateRole" @close="showRoleModal = false">
      <form @submit.prevent>
        <label>角色名称 <input v-model="roleForm.name" required placeholder="如：财务"></label>
        <label>权限模式
          <select v-model="roleForm.roleMode" required>
            <option value="whitelist">白名单（仅允许的权限可访问）</option>
            <option value="blacklist">黑名单（默认全部可访问，禁止指定权限）</option>
          </select>
        </label>
      </form>
    </ModalDialog>

    <ModalDialog v-if="showPermModal" title="新增权限" @submit="handleCreatePermission"
      @close="showPermModal = false">
      <form @submit.prevent>
        <label>方法
          <select v-model="permForm.method" required>
            <option value="GET">GET</option>
            <option value="POST">POST</option>
            <option value="PUT">PUT</option>
            <option value="DELETE">DELETE</option>
          </select>
        </label>
        <label>路径 <input v-model="permForm.pathPrefix" required placeholder="/api/employees"></label>
        <p class="perm-mode-hint">
          当前角色为{{ store.selectedRoleMode === 'blacklist' ? '黑名单' : '白名单' }}模式，
          此权限将标记为"<strong>{{ store.selectedModeLabel }}</strong>"
        </p>
      </form>
    </ModalDialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { usePermissionStore } from '@/stores/permissions'
import ModalDialog from '@/components/ModalDialog.vue'
import MasterDetailLayout from '@/layouts/MasterDetail.vue'

const store = usePermissionStore()
const showRoleModal = ref(false)
const showPermModal = ref(false)

const roleForm = reactive({ name: '', roleMode: 'whitelist' })
const permForm = reactive({ method: 'GET', pathPrefix: '' })

onMounted(() => {
  store.init()
})

async function handleCreateRole() {
  if (!roleForm.name.trim()) {
    alert('请输入角色名称')
    return
  }
  await store.createRole(roleForm.name, roleForm.roleMode)
  showRoleModal.value = false
  roleForm.name = ''
  roleForm.roleMode = 'whitelist'
}

async function handleCreatePermission() {
  if (!permForm.pathPrefix.trim()) {
    alert('请输入路径前缀')
    return
  }
  await store.createPermission(permForm.method, permForm.pathPrefix)
  showPermModal.value = false
  permForm.pathPrefix = ''
}

function confirmDeleteRole(name) {
  if (confirm(`确定要删除角色"${name}"吗？这将同时删除该角色的所有权限规则。`)) {
    store.deleteRole(name)
  }
}
</script>

<style scoped>
.role-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mode-badge {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 600;
  flex-shrink: 0;
}

.mode-badge-whitelist {
  background: #e6f4ea;
  color: #1e7e34;
}

.mode-badge-blacklist {
  background: #fce8e6;
  color: #c5221f;
}

.role-mode-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.tag-whitelist {
  background: #e6f4ea;
  color: #1e7e34;
}

.tag-blacklist {
  background: #fce8e6;
  color: #c5221f;
}

.perm-mode-hint {
  margin: var(--space-2) 0 0;
  font-size: 13px;
  color: var(--muted);
}

.perm-mode-hint strong {
  color: #333;
}
</style>
