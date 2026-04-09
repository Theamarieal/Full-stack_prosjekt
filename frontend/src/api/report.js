import api from './axios'

/**
 * Fetches a compliance report for a given date range.
 *
 * The report includes a summary of deviations, temperature logs,
 * checklist completions, and alcohol compliance registrations
 * within the specified period.
 *
 * @param {string} from - The start date of the report period in YYYY-MM-DD format.
 * @param {string} to - The end date of the report period in YYYY-MM-DD format.
 * @returns {Promise<Object>} The report data for the given date range.
 */
export async function fetchReport(from, to) {
  const response = await api.get('/reports', {
    params: { from, to },
  })
  return response.data
}
