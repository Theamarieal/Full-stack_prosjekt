import axios from './axios';

export default {
  getAll() {
    return axios.get('/deviations')
  },
  create(deviation) {
    return axios.post('/deviations', deviation)
  }
}
