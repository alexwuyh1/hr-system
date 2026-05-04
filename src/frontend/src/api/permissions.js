import apiClient from './index'

export const permissionsApi = {
  list: () => apiClient.get('/permissions'),
  roles: () => apiClient.get('/permissions/roles'),
  create: (data) => apiClient.post('/permissions', data),
  update: (id, data) => apiClient.put(`/permissions/${id}`, data),
  delete: (id) => apiClient.delete(`/permissions/${id}`),
  createRole: (data) => apiClient.post('/permissions/role', data),
  deleteRole: (name) => apiClient.delete(`/permissions/role/${encodeURIComponent(name)}`)
}
