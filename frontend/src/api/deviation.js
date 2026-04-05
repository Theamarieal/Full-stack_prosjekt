import axios from './axios';

export default {
  getAll() {
    return axios.get('/deviations')
  },
  create(deviation) {
    return axios.post('/deviations', deviation)
  },
  updateStatus(id, status) {
    return axios.patch(`/deviations/${id}/status`, null, { params: { status } })
  }
}
