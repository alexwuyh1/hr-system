import { defineStore } from 'pinia'
import { permissionsApi } from '@/api/permissions'

export const usePermissionStore = defineStore('permissions', {
  state: () => ({
    roles: [],
    permissions: [],
    selectedRole: null,
    selectedRoleMode: null,
    loading: false
  }),

  getters: {
    filteredPermissions: (state) => {
      if (!state.selectedRole) return []
      return state.permissions.filter(p => p.role === state.selectedRole)
    },
    selectedModeLabel: (state) => {
      return state.selectedRoleMode === 'blacklist' ? '禁止' : '允许'
    }
  },

  actions: {
    async init() {
      await this.loadRoles()
      if (!this.selectedRole && this.roles.length > 0) {
        this.selectRole(this.roles[0].role, this.roles[0].roleMode)
      }
      await this.loadPermissions()
    },

    async loadRoles() {
      this.roles = await permissionsApi.roles()
    },

    selectRole(role, roleMode) {
      this.selectedRole = role
      this.selectedRoleMode = roleMode
    },

    async loadPermissions() {
      this.loading = true
      try {
        this.permissions = await permissionsApi.list()
      } finally {
        this.loading = false
      }
    },

    async createRole(name, roleMode) {
      await permissionsApi.createRole({ role: name, roleMode })
      await this.loadRoles()
    },

    async deleteRole(name) {
      await permissionsApi.deleteRole(name)
      this.selectedRole = null
      this.selectedRoleMode = null
      await this.loadRoles()
      await this.loadPermissions()
    },

    async createPermission(method, pathPrefix) {
      await permissionsApi.create({
        role: this.selectedRole,
        method,
        pathPrefix
      })
      await this.loadPermissions()
    },

    async deletePermission(id) {
      await permissionsApi.delete(id)
      await this.loadPermissions()
    }
  }
})
