import axios from './axios'

/**
 * API service for checklist endpoints.
 *
 * Provides methods for fetching and completing checklists
 * within the IK-Mat and IK-Alkohol compliance modules.
 *
 * @module checklistApi
 */
export default {
  /**
   * Fetches a paginated list of checklists, with optional filtering by module and frequency.
   *
   * @param {number} [page=0] - The page number to retrieve (zero-indexed).
   * @param {number} [size=6] - The number of checklists per page.
   * @param {string|null} [module=null] - Filter by module type (e.g. 'FOOD', 'ALCOHOL'). Pass null or 'ALL' to skip.
   * @param {string|null} [frequency=null] - Filter by frequency (e.g. 'DAILY', 'WEEKLY'). Pass null or 'ALL' to skip.
   * @returns {Promise<Object>} Axios response containing a paginated list of checklists.
   */
  getAll(page = 0, size = 6, module = null, frequency = null) {
    const params = { page, size }
    if (module && module !== 'ALL') params.module = module
    if (frequency && frequency !== 'ALL') params.frequency = frequency
    return axios.get('/checklists', { params })
  },

  /**
   * Fetches all checklists in a single large page.
   * Intended for use cases where the full list is needed at once, such as the admin management view.
   *
   * @returns {Promise<Object>} Axios response containing up to 1000 checklists.
   */
  getLargePage() {
    return axios.get('/checklists', { params: { page: 0, size: 1000 } })
  },

  /**
   * Marks a specific checklist item as completed.
   *
   * @param {number} checklistId - The ID of the checklist containing the item.
   * @param {number} itemId - The ID of the checklist item to mark as complete.
   * @returns {Promise<Object>} Axios response confirming the completion.
   */
  completeItem(checklistId, itemId) {
    return axios.patch(`/checklists/${checklistId}/items/${itemId}/complete`)
  }
}
