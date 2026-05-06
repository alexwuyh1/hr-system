import { defineStore } from 'pinia'
import { dashboardApi } from '@/api/dashboard'
import { getCache, requestCache, clearCache } from '@/utils/cache'

const CACHE_TTL = 30000

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    data: null,
    loading: false
  }),

  actions: {
    async fetchSummary() {
      const cacheKey = 'dashboard/summary'
      const cached = getCache(cacheKey)
      if (cached) {
        this.data = cached
        return
      }

      this.loading = true
      try {
        this.data = await dashboardApi.summary()
        requestCache(cacheKey, this.data, CACHE_TTL)
      } finally {
        this.loading = false
      }
    }
  }
})