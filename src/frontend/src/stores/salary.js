import { defineStore } from 'pinia'
import { salaryApi } from '@/api/salary'

export const useSalaryStore = defineStore('salary', {
  state: () => ({
    list: [],
    loading: false
  }),

  actions: {
    async fetchList() {
      this.loading = true
      try {
        this.list = await salaryApi.list()
      } finally {
        this.loading = false
      }
    },

    async create(data) {
      await salaryApi.create(data)
      await this.fetchList()
    },

    async update(id, data) {
      await salaryApi.update(id, data)
      await this.fetchList()
    },

    async delete(id) {
      await salaryApi.delete(id)
      await this.fetchList()
    }
  }
})
