import api from '@/api/axios'

export async function getUsers() {
  const response = await api.get('/admin/users')
  return response.data
}

export async function createUser(userData) {
  const response = await api.post('/admin/users', userData)
  return response.data
}

export async function updateUserRole(userId, role) {
  const response = await api.patch(`/admin/users/${userId}/role`, { role })
  return response.data
}
