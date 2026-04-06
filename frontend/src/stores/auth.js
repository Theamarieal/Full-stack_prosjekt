import { defineStore } from 'pinia'
import api from '@/api/axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: sessionStorage.getItem('token') || null,
    user: JSON.parse(sessionStorage.getItem('user')) || null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    getUserRole: (state) => state.user?.role || null,
  },

  actions: {
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

    logout() {
      this.token = null
      this.user = null
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
    },
  },
})
