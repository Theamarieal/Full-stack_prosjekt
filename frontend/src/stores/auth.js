import { defineStore } from 'pinia'
import api from '@/api/axios'

/**
 * Pinia store for authentication state.
 *
 * Manages the current user's session, including JWT token storage,
 * login, registration, and logout. Session data is persisted in sessionStorage
 * so that it survives page refreshes but is cleared when the browser tab is closed.
 *
 * @store auth
 */
export const useAuthStore = defineStore('auth', {
  /**
   * @returns {{ token: string|null, user: Object|null }}
   */
  state: () => ({
    /** @type {string|null} The JWT token for the current session. */
    token: sessionStorage.getItem('token') || null,

    /** @type {Object|null} The authenticated user object, including role and identity. */
    user: JSON.parse(sessionStorage.getItem('user')) || null,
  }),

  getters: {
    /**
     * Whether a user is currently logged in.
     *
     * @param {Object} state
     * @returns {boolean}
     */
    isLoggedIn: (state) => !!state.token,

    /**
     * The role of the currently authenticated user, or null if not logged in.
     *
     * @param {Object} state
     * @returns {string|null} Role string (e.g. 'EMPLOYEE', 'MANAGER', 'ADMIN').
     */
    getUserRole: (state) => state.user?.role || null,
  },

  actions: {
    /**
     * Authenticates a user with the given credentials.
     * On success, stores the JWT token and user object in state and sessionStorage.
     *
     * @param {string} email - The user's email address.
     * @param {string} password - The user's password.
     * @returns {Promise<boolean>} True if login succeeded, false otherwise.
     */
    async login(email, password) {
      try {
        const response = await api.post('/auth/login', { email, password })

        this.token = response.data.token
        this.user = response.data.user

        sessionStorage.setItem('token', this.token)
        sessionStorage.setItem('user', JSON.stringify(this.user))

        return true
      } catch (error) {
        console.error('Login failed:', error.response?.data || error.message)
        return false
      }
    },

    /**
     * Registers a new user account.
     * On success, stores the JWT token and user object in state and sessionStorage.
     *
     * @param {string} email - The email address for the new account.
     * @param {string} password - The password for the new account.
     * @param {string} role - The role to assign (e.g. 'EMPLOYEE', 'MANAGER').
     * @param {number} [organizationId=1] - The ID of the organization to register under.
     * @returns {Promise<{ success: boolean, message?: string }>} Result object indicating success or failure.
     */
    async register(email, password, role, organizationId = 1) {
      try {
        const response = await api.post('/auth/register', {
          email,
          password,
          role,
          organizationId,
        })

        this.token = response.data.token
        this.user = response.data.user

        sessionStorage.setItem('token', this.token)
        sessionStorage.setItem('user', JSON.stringify(this.user))

        return { success: true }
      } catch (error) {
        console.error('Registration failed:', error.response?.data || error.message)

        const message =
          error.response?.data?.message || error.response?.data || 'Registration failed.'

        return {
          success: false,
          message,
        }
      }
    },

    /**
     * Logs out the current user by clearing state and sessionStorage.
     */
    logout() {
      this.token = null
      this.user = null
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
    },
  },
})
