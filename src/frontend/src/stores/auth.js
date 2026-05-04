import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('hr_token') || null,
    currentRole: localStorage.getItem('hr_role') || null,
    isAuthenticated: !!localStorage.getItem('hr_token')
  }),

  actions: {
    init() {
      if (this.token) {
        this.fetchUserInfo()
      }
    },

    async login(username, password) {
      const result = await authApi.login({ username, password })
      this.token = result.token
      this.currentRole = result.role
      this.isAuthenticated = true
      localStorage.setItem('hr_token', this.token)
      localStorage.setItem('hr_role', this.currentRole)
    },

    async fetchUserInfo() {
      try {
        const user = await authApi.getMe()
        this.currentRole = user.role
        localStorage.setItem('hr_role', this.currentRole)
      } catch {
        this.logout()
      }
    },

    logout() {
      this.token = null
      this.currentRole = null
      this.isAuthenticated = false
      localStorage.removeItem('hr_token')
      localStorage.removeItem('hr_role')
    }
  }
})
