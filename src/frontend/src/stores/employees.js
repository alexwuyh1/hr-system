import { defineStore } from 'pinia'
import { employeesApi } from '@/api/employees'
import { getCache, requestCache, clearCache, generateCacheKey } from '@/utils/cache'

const CACHE_TTL = 30000

export const useEmployeeStore = defineStore('employees', {
  state: () => ({
    list: [],
    loading: false
  }),

  getters: {
    activeEmployees: (state) => state.list.filter(e => e.status === '在职')
  },

  actions: {
    async fetchList(useCache = true) {
      const cacheKey = generateCacheKey('employees/list')
      if (useCache) {
        const cached = getCache(cacheKey)
        if (cached) {
          this.list = cached
          return
        }
      }

      this.loading = true
      try {
        this.list = await employeesApi.list()
        requestCache(cacheKey, this.list, CACHE_TTL)
      } finally {
        this.loading = false
      }
    },

    async create(data) {
      await employeesApi.create(data)
      clearCache('employees/list')
      await this.fetchList(false)
    },

    async update(id, data) {
      await employeesApi.update(id, data)
      clearCache('employees/list')
      await this.fetchList(false)
    },

    async delete(id) {
      await employeesApi.delete(id)
      clearCache('employees/list')
      await this.fetchList(false)
    },

    async toggleStatus(employeeNo, isActive) {
      const api = isActive ? employeesApi.resign : employeesApi.rehire
      await api({ employeeNo })
      clearCache('employees/list')
      await this.fetchList(false)
    },

    async uploadAvatar(id, file) {
      const updated = await employeesApi.uploadAvatar(id, file)
      const index = this.list.findIndex(e => e.id === id)
      if (index !== -1) {
        this.list[index] = updated
      }
      clearCache('employees/list')
    }
  }
})