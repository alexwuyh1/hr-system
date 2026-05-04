import apiClient from './index'

export const organizationApi = {
  list: () => apiClient.get('/organizations'),
  positionTree: () => apiClient.get('/organizations/position-tree'),
  create: (data) => apiClient.post('/organizations', data),
  update: (id, data) => apiClient.put(`/organizations/${id}`, data),
  delete: (id) => apiClient.delete(`/organizations/${id}`),
}
