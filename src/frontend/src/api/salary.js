import apiClient from './index'

export const salaryApi = {
  list: () => apiClient.get('/salaries'),
  create: (data) => apiClient.post('/salaries', data),
  update: (id, data) => apiClient.put(`/salaries/${id}`, data),
  delete: (id) => apiClient.delete(`/salaries/${id}`),
}
