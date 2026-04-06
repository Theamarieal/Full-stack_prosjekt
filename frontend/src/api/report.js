import api from './axios'

export async function fetchReport(from, to) {
  const response = await api.get('/reports', {
    params: { from, to },
  })
  return response.data
}
