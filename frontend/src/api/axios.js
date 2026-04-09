import axios from 'axios';

/**
 * Pre-configured Axios instance for communicating with the Checkd backend API.
 *
 * Base URL: http://localhost:8080/api/v1
 *
 * Includes two interceptors:
 * - Request interceptor: attaches the JWT token from sessionStorage as a Bearer token.
 * - Response interceptor: handles 401 Unauthorized responses by clearing session
 *   storage and redirecting the user to the login page.
 *
 * @module axios
 */
const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

/**
 * Request interceptor that attaches the JWT token to every outgoing request.
 * The token is retrieved from sessionStorage under the key 'token'.
 */
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

/**
 * Response interceptor for global error handling.
 * If a 401 Unauthorized response is received and a token exists in sessionStorage,
 * the session is cleared and the user is redirected to the login page.
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const token = sessionStorage.getItem('token');
      if (token) {
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('user');
        window.location.replace('http://localhost:5173/login');
        return new Promise(() => {});
      }
    }
    return Promise.reject(error);
  }
);

export default api;
