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
  (response) => {
    console.log("response status : ", response.status);
    // console.log("response data : ", response.data);
    return response;
  },
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
    
    const response = await api.post('/upload', formData, {
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
    const response = await api.get('/list', {
      params: { page, size, sort: 'uploadTimestamp,desc', _t: new Date().getTime()},
    });
    return response.data;
  },

  // Get file metadata
  getFileMetadata: async (fileId) => {
    const response = await api.get(`/${fileId}/metadata`);
    return response.data;
  },

  // Download file
  downloadFile: async (fileId) => {
    const response = await api.get(`/${fileId}/download`, {
      responseType: 'blob',
    });
    return response;
  },

  // Get file content for viewing
  getFileContent: async (fileId) => {
    const response = await api.get(`/${fileId}/view`, {
      responseType: 'text',
    });
    return response.data;
  },

  // Delete file
  deleteFile: async (fileId) => {
    const response = await api.delete(`/${fileId}`);
    return response.data;
  },
};

export default api;