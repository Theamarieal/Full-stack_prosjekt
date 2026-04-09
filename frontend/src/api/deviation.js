import axios from './axios';

/**
 * API service for deviation endpoints.
 *
 * Provides methods for creating, retrieving, and updating deviations
 * across both the IK-Mat and IK-Alkohol compliance modules.
 *
 * @module deviationApi
 */
export default {
  /**
   * Fetches a paginated list of deviations for the current organization.
   *
   * @param {number} [page=0] - The page number to retrieve (zero-indexed).
   * @param {number} [size=5] - The number of deviations per page.
   * @returns {Promise<Object>} Axios response containing a paginated list of deviations.
   */
  getAll(page = 0, size = 5) {
    return axios.get('/deviations', { params: { page, size } })
  },

  /**
   * Fetches all deviations in a single large page.
   * Intended for use cases where the full list is needed at once, such as report generation.
   *
   * @returns {Promise<Object>} Axios response containing up to 1000 deviations.
   */
  getLargePage() {
    return axios.get('/deviations', { params: { page: 0, size: 1000 } })
  },

  /**
   * Creates a new deviation report.
   *
   * @param {Object} deviation - The deviation data.
   * @param {string} deviation.title - Short title describing the deviation.
   * @param {string} deviation.description - Detailed description of the deviation.
   * @param {string} deviation.module - The compliance module (e.g. 'FOOD', 'ALCOHOL').
   * @returns {Promise<Object>} Axios response containing the created deviation.
   */
  create(deviation) {
    return axios.post('/deviations', deviation)
  },

  /**
   * Updates the status of an existing deviation.
   * Only accessible to users with the MANAGER or ADMIN role.
   *
   * @param {number} id - The ID of the deviation to update.
   * @param {string} status - The new status (e.g. 'OPEN', 'IN_PROGRESS', 'CLOSED').
   * @returns {Promise<Object>} Axios response containing the updated deviation.
   */
  updateStatus(id, status) {
    return axios.patch(`/deviations/${id}/status`, null, { params: { status } })
  }
}
