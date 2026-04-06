import api from './axios'

export default {
  getAllEquipment() {
    return api.get('/equipment')
  },
}
