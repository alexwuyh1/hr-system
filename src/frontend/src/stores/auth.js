import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { secureStorage } from '@/utils/storage'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: secureStorage.getItem('token'),
    currentRole: secureStorage.getItem('role'),
    isAuthenticated: !!secureStorage.getItem('token')
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
      secureStorage.setItem('token', this.token)
      secureStorage.setItem('role', this.currentRole)
    },

    async fetchUserInfo() {
      try {
        const user = await authApi.getMe()
        this.currentRole = user.role
        secureStorage.setItem('role', this.currentRole)
      } catch {
        this.logout()
      }
    },

    logout() {
      this.token = null
      this.currentRole = null
      this.isAuthenticated = false
      secureStorage.removeItem('token')
      secureStorage.removeItem('role')
    }
  }
})