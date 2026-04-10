import api from '@/api/axios'

/**
 * Fetches all users in the current organization.
 *
 * @returns {Promise<Array>} A list of user objects.
 */
export async function getUsers() {
  const response = await api.get('/admin/users')
  return response.data
}

/**
 * Creates a new user in the current organization.
 *
 * @param {Object} userData - The user data for the new account.
 * @param {string} userData.firstName - First name of the user.
 * @param {string} userData.lastName - Last name of the user.
 * @param {string} userData.email - Email address of the user.
 * @param {string} userData.password - Initial password for the user.
 * @param {string} userData.role - Role assigned to the user (e.g. EMPLOYEE, MANAGER).
 * @returns {Promise<Object>} The created user object.
 */
export async function createUser(userData) {
  const response = await api.post('/admin/users', userData)
  return response.data
}

/**
 * Updates the role of an existing user.
 *
 * @param {number} userId - The ID of the user to update.
 * @param {string} role - The new role to assign (e.g. EMPLOYEE, MANAGER, ADMIN).
 * @returns {Promise<Object>} The updated user object.
 */
export async function updateUserRole(userId, role) {
  const response = await api.patch(`/admin/users/${userId}/role`, { role })
  return response.data
}

/**
 * Toggles the active status of a user account.
 *
 * @param {number} userId - The ID of the user to update.
 * @param {boolean} active - Whether the user account should be active or inactive.
 * @returns {Promise<Object>} The updated user object.
 */
export async function toggleUserActive(userId, active) {
  const response = await api.patch(`/admin/users/${userId}/active`, { active })
  return response.data
}

/**
 * Deletes a user from the current organization.
 *
 * @param {number} userId - The ID of the user to delete.
 * @returns {Promise<void>}
 */
export async function deleteUser(userId) {
  await api.delete(`/admin/users/${userId}`)
}

/**
 * Creates a new organization.
 *
 * @param {Object} data - The organization data.
 * @param {string} data.name - The name of the organization.
 * @returns {Promise<Object>} The created organization object.
 */
export async function createOrganization(data) {
  const response = await api.post('/admin/organizations', data)
  return response.data
}
