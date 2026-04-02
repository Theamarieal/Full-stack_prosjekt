import { defineStore } from 'pinia';
import api from '@/api/axios';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // we fetched saved data from sessionStorage if it exists (for example after a refresh)
    token: sessionStorage.getItem('token') || null,
    user: JSON.parse(sessionStorage.getItem('user')) || null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    getUserRole: (state) => state.user?.role || null,
  },

  actions: {
    // login
    async login(email, password) {
      try {
        const response = await api.post('/auth/login', { email, password });

        this.token = response.data.token;
        this.user = response.data.user;

        sessionStorage.setItem('token', this.token);
        sessionStorage.setItem('user', JSON.stringify(this.user));
        return true;
      } catch (error) {
        console.error('Innlogging feilet:', error.response?.data || error.message);
        return false;
      }
    },

    // register (signup)
    async register(email, password, role, organizationId = 1) {
      try {
        // ends with organizationId (standardvalue 1 for the test-org)
        const response = await api.post('/auth/register', { 
          email, 
          password, 
          role, 
          organizationId 
        });

        // with sucessfull signup we log the user straight in
        this.token = response.data.token;
        this.user = response.data.user;

        sessionStorage.setItem('token', this.token);
        sessionStorage.setItem('user', JSON.stringify(this.user));
        return true;
      } catch (error) {
        console.error('Registrering feilet:', error.response?.data || error.message);
        return false;
      }
    },

    // logout
    logout() {
      this.token = null;
      this.user = null;
      sessionStorage.removeItem('token');
      sessionStorage.removeItem('user');
    }
  }
});