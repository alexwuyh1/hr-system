import { defineStore } from 'pinia'
import { dashboardApi } from '@/api/dashboard'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    data: null,
    loading: false
  }),

  actions: {
    async fetchSummary() {
      this.loading = true
      try {
        this.data = await dashboardApi.summary()
      } finally {
        this.loading = false
      }
    }
  }
})
