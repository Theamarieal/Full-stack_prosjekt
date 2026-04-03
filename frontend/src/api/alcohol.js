import api from './axios'

/**
 * API wrapper for alcohol log endpoints.
 */
const alcoholApi = {
  /**
   * Creates a new alcohol log entry.
   *
   * @param {Object} payload - The alcohol log payload.
   * @returns {Promise<any>} Axios response containing the saved log.
   */
  createLog(payload) {
    return api.post('/alcohol-logs', payload)
  },

  /**
   * Fetches alcohol log history for the current day.
   *
   * @returns {Promise<any>} Axios response containing current-day logs.
   */
  getCurrentShiftLogs() {
    return api.get('/alcohol-logs/current-shift')
  },

  /**
   * Fetches alcohol log history for a specific date.
   *
   * <p>This endpoint is intended for managers and administrators.
   *
   * @param {string} date - The date in YYYY-MM-DD format.
   * @returns {Promise<any>} Axios response containing filtered logs.
   */
  getLogsByDate(date) {
    return api.get('/alcohol-logs', {
      params: { date },
    })
  },

  /**
   * Fetches alcohol registration status for the current day.
   *
   * @returns {Promise<any>} Axios response containing dashboard status information.
   */
  getAlcoholStatus() {
    return api.get('/alcohol-logs/status')
  },
}

export default alcoholApi
