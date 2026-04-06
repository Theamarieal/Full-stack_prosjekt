import api from './axios'

export default {
  createTemperatureLog(equipmentId, value) {
    return api.post('/temperature-logs', null, {
      params: { equipmentId, value },
    })
  },

  getLatestTemperatureLogs(limit = 10) {
    return api.get('/temperature-logs/latest', {
      params: { limit },
    })
  },

  getTemperatureHistory(params) {
    return api.get('/temperature-logs/history', { params })
  },

  getTemperatureSummary() {
    return api.get('/temperature-logs/summary')
  },

  getLatestDeviations(limit = 5) {
    return api.get('/temperature-logs/deviations/latest', {
      params: { limit },
    })
  },
}
