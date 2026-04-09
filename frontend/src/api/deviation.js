import axios from './axios';

export default {
  getAll(page = 0, size = 5) {
    return axios.get('/deviations', { params: { page, size } })
  },
  getLargePage() {
    return axios.get('/deviations', { params: { page: 0, size: 1000 } })
  },
  create(deviation) {
    return axios.post('/deviations', deviation)
  },
  updateStatus(id, status) {
    return axios.patch(`/deviations/${id}/status`, null, { params: { status } })
  }
}
