import { defineStore } from 'pinia'
import { employeesApi } from '@/api/employees'

export const useEmployeeStore = defineStore('employees', {
  state: () => ({
    list: [],
    loading: false
  }),

  getters: {
    activeEmployees: (state) => state.list.filter(e => e.status === '在职')
  },

  actions: {
    async fetchList() {
      this.loading = true
      try {
        this.list = await employeesApi.list()
      } finally {
        this.loading = false
      }
    },

    async create(data) {
      await employeesApi.create(data)
      await this.fetchList()
    },

    async update(id, data) {
      await employeesApi.update(id, data)
      await this.fetchList()
    },

    async delete(id) {
      await employeesApi.delete(id)
      await this.fetchList()
    },

    async toggleStatus(employeeNo, isActive) {
      const api = isActive ? employeesApi.resign : employeesApi.rehire
      await api({ employeeNo })
      await this.fetchList()
    },

    async uploadAvatar(id, file) {
      await employeesApi.uploadAvatar(id, file)
      await this.fetchList()
    }
  }
})
