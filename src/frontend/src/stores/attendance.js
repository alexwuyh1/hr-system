import { defineStore } from 'pinia'
import { attendanceApi, attendanceRulesApi, faceApi } from '@/api/attendance'

export const useAttendanceStore = defineStore('attendance', {
  state: () => ({
    list: [],
    rule: null,
    loading: false
  }),

  actions: {
    async fetchList() {
      this.loading = true
      try {
        this.list = await attendanceApi.list()
      } finally {
        this.loading = false
      }
    },

    async fetchRule() {
      this.rule = await attendanceRulesApi.get()
    },

    async create(data) {
      await attendanceApi.create(data)
      await this.fetchList()
    },

    async update(id, data) {
      await attendanceApi.update(id, data)
      await this.fetchList()
    },

    async delete(id) {
      await attendanceApi.delete(id)
      await this.fetchList()
    },

    async updateRule(data) {
      await attendanceRulesApi.update(data)
      await this.fetchRule()
    },

    async faceAttendance(employeeId, file) {
      return await faceApi.attendance(employeeId, file)
    }
  }
})
