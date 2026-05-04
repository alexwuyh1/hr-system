import apiClient from './index'

export const dashboardApi = {
  summary: () => apiClient.get('/dashboard/summary')
}

export const importExportApi = {
  export: (type, format) => apiClient.get(`/data/export/${type}?format=${format}`, { responseType: 'blob' }),
  import: (type, file) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient.post(`/data/import/${type}`, form, { headers: { 'Content-Type': 'multipart/form-data' } })
  }
}
