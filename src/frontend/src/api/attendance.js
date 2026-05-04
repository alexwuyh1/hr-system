import apiClient from './index'

export const attendanceApi = {
  list: () => apiClient.get('/attendance'),
  create: (data) => apiClient.post('/attendance', data),
  update: (id, data) => apiClient.put(`/attendance/${id}`, data),
  delete: (id) => apiClient.delete(`/attendance/${id}`),
}

export const attendanceRulesApi = {
  get: () => apiClient.get('/attendance-rules'),
  update: (data) => apiClient.put('/attendance-rules', data),
}

export const faceApi = {
  attendance: (employeeId, file) => {
    const form = new FormData()
    form.append('employeeId', employeeId)
    form.append('file', file)
    return apiClient.post('/face/attendance', form, { headers: { 'Content-Type': 'multipart/form-data' } })
  }
}
