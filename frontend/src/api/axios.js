import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json'
  }
});

// interceptor that adds JWT-token in each request
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

// response interceptor for error handling
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
