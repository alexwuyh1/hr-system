import { defineStore } from 'pinia'
import { attendanceApi, attendanceRulesApi, faceApi } from '@/api/attendance'
import { getCache, requestCache, clearCache, generateCacheKey } from '@/utils/cache'

const CACHE_TTL = 30000

export const useAttendanceStore = defineStore('attendance', {
  state: () => ({
    list: [],
    rule: null,
    loading: false
  }),

  actions: {
    async fetchList(useCache = true) {
      const cacheKey = generateCacheKey('attendance/list')
      if (useCache) {
        const cached = getCache(cacheKey)
        if (cached) {
          this.list = cached
          return
        }
      }

      this.loading = true
      try {
        this.list = await attendanceApi.list()
        requestCache(cacheKey, this.list, CACHE_TTL)
      } finally {
        this.loading = false
      }
    },

    async fetchRule() {
      const cacheKey = 'attendance/rule'
      const cached = getCache(cacheKey)
      if (cached) {
        this.rule = cached
        return
      }
      this.rule = await attendanceRulesApi.get()
      requestCache(cacheKey, this.rule, CACHE_TTL)
    },

    async create(data) {
      await attendanceApi.create(data)
      clearCache('attendance/list')
      await this.fetchList(false)
    },

    async update(id, data) {
      await attendanceApi.update(id, data)
      clearCache('attendance/list')
      await this.fetchList(false)
    },

    async delete(id) {
      await attendanceApi.delete(id)
      clearCache('attendance/list')
      await this.fetchList(false)
    },

    async updateRule(data) {
      await attendanceRulesApi.update(data)
      clearCache('attendance/rule')
      await this.fetchRule()
    },

    async faceAttendance(employeeId, file) {
      return await faceApi.attendance(employeeId, file)
    }
  }
})