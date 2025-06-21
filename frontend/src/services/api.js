import axios from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

// Request interceptor for logging
api.interceptors.request.use(
  (config) => {
    console.log('API Request:', config.method?.toUpperCase(), config.url);
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export const fileService = {
  // Upload file
  uploadFile: async (file, onProgress) => {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        );
        onProgress?.(percentCompleted);
      },
    });
    
    return response.data;
  },

  // Get all files
  getAllFiles: async (page = 0, size = 20) => {
    const response = await api.get('/files', {
      params: { page, size, sort: 'uploadTimestamp,desc' },
    });
    return response.data;
  },

  // Get file metadata
  getFileMetadata: async (fileId) => {
    const response = await api.get(`/files/${fileId}/metadata`);
    return response.data;
  },

  // Download file
  downloadFile: async (fileId) => {
    const response = await api.get(`/files/${fileId}/download`, {
      responseType: 'blob',
    });
    return response;
  },

  // Get file content for viewing
  getFileContent: async (fileId) => {
    const response = await api.get(`/files/${fileId}/download`, {
      responseType: 'text',
    });
    return response.data;
  },

  // Delete file
  deleteFile: async (fileId) => {
    const response = await api.delete(`/files/${fileId}`);
    return response.data;
  },
};

export default api;