import api from './axios'

/**
 * API service for alcohol compliance log endpoints.
 *
 * Provides methods for creating and retrieving alcohol log entries
 * as part of the IK-Alkohol compliance module.
 *
 * @module alcoholApi
 */
const alcoholApi = {
  /**
   * Creates a new alcohol log entry for the current shift.
   *
   * @param {Object} payload - The alcohol log payload.
   * @param {string} payload.type - The log type (e.g. OPENING, CLOSING, SPOT_CHECK).
   * @param {string} [payload.notes] - Optional notes for the log entry.
   * @returns {Promise<Object>} Axios response containing the saved log.
   */
  createLog(payload) {
    return api.post('/alcohol-logs', payload)
  },

  /**
   * Fetches alcohol log entries for the current shift (today).
   *
   * @returns {Promise<Object>} Axios response containing current-day logs.
   */
  getCurrentShiftLogs() {
    return api.get('/alcohol-logs/current-shift')
  },

  /**
   * Fetches alcohol log entries for a specific date.
   * Intended for use by managers and administrators.
   *
   * @param {string} date - The date to filter by, in YYYY-MM-DD format.
   * @returns {Promise<Object>} Axios response containing logs for the given date.
   */
  getLogsByDate(date) {
    return api.get('/alcohol-logs', {
      params: { date },
    })
  },

  /**
   * Fetches the alcohol compliance status for the current day.
   * Used by the dashboard to show whether required registrations are complete.
   *
   * @returns {Promise<Object>} Axios response containing dashboard status information.
   */
  getAlcoholStatus() {
    return api.get('/alcohol-logs/status')
  },
}

export default alcoholApi
