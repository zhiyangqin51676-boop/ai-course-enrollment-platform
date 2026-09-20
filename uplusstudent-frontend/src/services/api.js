import axios from 'axios';

// Use environment variable or fallback to localhost for development
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || '/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: async (credentials) => {
    const response = await api.post('/login', credentials);
    return response.data;
  },
};

export const coursesAPI = {
  getAllCourses: async () => {
    const response = await api.get('/courses');
    return response.data;
  },
  
  getAvailableCourses: async (studentId) => {
    const response = await api.get(`/courses?studentId=${studentId}`);
    return response.data;
  },
  
  enrollInCourse: async (courseId, studentId) => {
    const response = await api.post(`/courses/${courseId}/enroll?studentId=${studentId}`);
    return response.data;
  },
  
  getMyEnrollments: async (studentId) => {
    const response = await api.get(`/me/courses?studentId=${studentId}`);
    return response.data;
  },
  
  dropCourse: async (courseId, studentId) => {
    const response = await api.delete(`/courses/${courseId}/drop?studentId=${studentId}`);
    return response.data;
  },
};

export default api;