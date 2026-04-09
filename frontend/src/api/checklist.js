import axios from './axios'

export default {
  getAll(page = 0, size = 6, module = null, frequency = null) {
    const params = { page, size }
    if (module && module !== 'ALL') params.module = module
    if (frequency && frequency !== 'ALL') params.frequency = frequency
    return axios.get('/checklists', { params })
  },
  getLargePage() {
    return axios.get('/checklists', { params: { page: 0, size: 1000 } })
  },
  completeItem(checklistId, itemId) {
    return axios.patch(`/checklists/${checklistId}/items/${itemId}/complete`)
  }
}
