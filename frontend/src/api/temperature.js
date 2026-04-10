import api from './axios'

/**
 * API service for temperature logging endpoints.
 *
 * Provides methods for recording and retrieving temperature logs
 * as part of the IK-Mat food safety compliance module.
 *
 * @module temperatureApi
 */
export default {
  /**
   * Records a new temperature log entry for a specific piece of equipment.
   *
   * @param {number} equipmentId - The ID of the equipment being logged.
   * @param {number} value - The recorded temperature value in degrees Celsius.
   * @returns {Promise<Object>} Axios response containing the saved temperature log.
   */
  createTemperatureLog(equipmentId, value) {
    return api.post('/temperature-logs', null, {
      params: { equipmentId, value },
    })
  },

  /**
   * Fetches the most recent temperature log entries across all equipment.
   *
   * @param {number} [limit=10] - Maximum number of log entries to retrieve.
   * @returns {Promise<Object>} Axios response containing the latest temperature logs.
   */
  getLatestTemperatureLogs(limit = 10) {
    return api.get('/temperature-logs/latest', {
      params: { limit },
    })
  },

  /**
   * Fetches historical temperature log entries with optional filtering.
   *
   * @param {Object} params - Query parameters for filtering the history.
   * @param {string} [params.from] - Start date in YYYY-MM-DD format.
   * @param {string} [params.to] - End date in YYYY-MM-DD format.
   * @param {number} [params.equipmentId] - Filter by a specific equipment ID.
   * @returns {Promise<Object>} Axios response containing filtered temperature history.
   */
  getTemperatureHistory(params) {
    return api.get('/temperature-logs/history', { params })
  },

  /**
   * Fetches a summary of temperature status across all equipment.
   * Used by the dashboard to indicate whether temperatures are within acceptable ranges.
   *
   * @returns {Promise<Object>} Axios response containing the temperature summary.
   */
  getTemperatureSummary() {
    return api.get('/temperature-logs/summary')
  },

  /**
   * Fetches the most recent temperature deviation entries.
   *
   * @param {number} [limit=5] - Maximum number of deviation entries to retrieve.
   * @returns {Promise<Object>} Axios response containing the latest temperature deviations.
   */
  getLatestDeviations(limit = 5) {
    return api.get('/temperature-logs/deviations/latest', {
      params: { limit },
    })
  },
}
