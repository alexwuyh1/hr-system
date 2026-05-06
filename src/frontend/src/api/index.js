import axios from 'axios'
import { secureStorage } from '@/utils/storage'

const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000
})

let isRefreshing = false
let refreshSubscribers = []

function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

function onTokenRefreshed(token) {
  refreshSubscribers.forEach(callback => callback(token))
  refreshSubscribers = []
}

apiClient.interceptors.request.use((config) => {
  const token = secureStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.retry = config.retry || 3
  config.retryDelay = config.retryDelay || 1000
  return config
})

apiClient.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          subscribeTokenRefresh((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(apiClient(originalRequest))
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = secureStorage.getItem('refresh_token')
        if (refreshToken) {
          const res = await axios.post('/api/auth/refresh', { refreshToken })
          const { token } = res.data
          secureStorage.setItem('token', token)
          onTokenRefreshed(token)
          originalRequest.headers.Authorization = `Bearer ${token}`
          return apiClient(originalRequest)
        }
      } catch {
        secureStorage.removeItem('token')
        secureStorage.removeItem('role')
        window.location.href = '/'
      } finally {
        isRefreshing = false
        refreshSubscribers = []
      }
    }

    const retry = originalRequest.retry
    const retryDelay = originalRequest.retryDelay

    if (retry > 0 && !originalRequest._retry) {
      originalRequest._retry = true
      originalRequest.retry = retry - 1
      await new Promise(resolve => setTimeout(resolve, retryDelay))
      return apiClient(originalRequest)
    }

    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      alert('请求超时，请重试')
    } else {
      const msg = error.response?.data?.message || error.message || '请求失败'
      alert(msg)
    }
    return Promise.reject(error)
  }
)

export default apiClient