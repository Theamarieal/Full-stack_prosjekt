import axios from './axios'

export default {
  getAll() {
    return axios.get('/checklists')
  },
  completeItem(checklistId,itemId) {
    return axios.patch(`/checklists/${checklistId}/items/${itemId}/complete`)
  }
}
