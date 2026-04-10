import api from './axios'

/**
 * API service for equipment endpoints.
 *
 * Provides methods for retrieving equipment registered in the system,
 * used in the context of temperature logging within the IK-Mat module.
 *
 * @module equipmentApi
 */
export default {
  /**
   * Fetches all equipment registered for the current organization.
   *
   * @returns {Promise<Object>} Axios response containing a list of equipment objects.
   */
  getAllEquipment() {
    return api.get('/equipment')
  },
}
