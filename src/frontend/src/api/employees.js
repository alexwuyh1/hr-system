import apiClient from './index'

export const employeesApi = {
  list: () => apiClient.get('/employees'),
  create: (data) => apiClient.post('/employees', data),
  update: (id, data) => apiClient.put(`/employees/${id}`, data),
  delete: (id) => apiClient.delete(`/employees/${id}`),
  resign: (data) => apiClient.post('/employees/resign', data),
  rehire: (data) => apiClient.post('/employees/rehire', data),
  uploadAvatar: (id, file) => {
    const form = new FormData()
    form.append('file', file)
    return apiClient.post(`/employees/${id}/avatar`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
