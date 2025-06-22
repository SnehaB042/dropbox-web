import React, { useState, useRef } from 'react';
import { useFileUpload } from '../../hooks/useFiles';
import { X, Upload, FileText, Image, AlertCircle, CheckCircle } from 'lucide-react';
import { formatFileSize } from '../../utils/fileUtils';

const FileUpload = ({ onClose }) => {
  const [dragActive, setDragActive] = useState(false);
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [uploadProgress, setUploadProgress] = useState({});
  const [uploadStatus, setUploadStatus] = useState({}); // 'uploading', 'success', 'error'
  const fileInputRef = useRef(null);
  
  const uploadMutation = useFileUpload();

  const allowedTypes = ['text/plain', 'image/jpeg', 'image/png', 'application/json'];
  const maxFileSize = 10 * 1024 * 1024; // 10MB

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    
    const droppedFiles = Array.from(e.dataTransfer.files);
    handleFiles(droppedFiles);
  };

  const handleFileSelect = (e) => {
    const files = Array.from(e.target.files);
    handleFiles(files);
  };

  const handleFiles = (files) => {
    const validFiles = files.filter(file => {
      if (!allowedTypes.includes(file.type)) {
        alert(`File type ${file.type} is not supported`);
        return false;
      }
      if (file.size > maxFileSize) {
        alert(`File ${file.name} is too large (max 10MB)`);
        return false;
      }
      return true;
    });

    setSelectedFiles(prev => [...prev, ...validFiles]);
  };

  const removeFile = (index) => {
    setSelectedFiles(prev => prev.filter((_, i) => i !== index));
  };

  const uploadFile = async (file, index) => {
    const fileKey = `${index}-${file.name}`;
    
    try {
      setUploadStatus(prev => ({ ...prev, [fileKey]: 'uploading' }));
      
      await uploadMutation.mutateAsync({
        file,
        onProgress: (progress) => {
          setUploadProgress(prev => ({ ...prev, [fileKey]: progress }));
        }
      });
      
      setUploadStatus(prev => ({ ...prev, [fileKey]: 'success' }));
      
      // Remove successful uploads after a delay
      setTimeout(() => {
        setSelectedFiles(prev => prev.filter((_, i) => i !== index));
        setUploadProgress(prev => {
          const newProgress = { ...prev };
          delete newProgress[fileKey];
          return newProgress;
        });
        setUploadStatus(prev => {
          const newStatus = { ...prev };
          delete newStatus[fileKey];
          return newStatus;
        });
      }, 1000);
      
    } catch (error) {
      console.log(error);
      setUploadStatus(prev => ({ ...prev, [fileKey]: 'error' }));
    }
  };

  const uploadAllFiles = () => {
    selectedFiles.forEach((file, index) => {
      const fileKey = `${index}-${file.name}`;
      if (!uploadStatus[fileKey]) {
        uploadFile(file, index);
      }
    });
  };

  const getFileIcon = (contentType, className = 'w-6 h-6') => {
  if (!contentType) {
    return <File className={`${className} text-gray-400`} />;
  }
  
  if (contentType === 'text/plain') {
    return <FileText className={`${className} text-blue-600`} />;
  }
  
  if (contentType.startsWith('image/')) {
    return <Image className={`${className} text-green-600`} />;
  }
  
  if (contentType === 'application/json') {
    return <Code className={`${className} text-purple-600`} />;
  }
  
  return <File className={`${className} text-gray-400`} />;
};

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold">Upload Files</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X size={24} />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Drop Zone */}
          <div
            className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
              dragActive 
                ? 'border-primary-500 bg-primary-50' 
                : 'border-gray-300 hover:border-gray-400'
            }`}
            onDragEnter={handleDrag}
            onDragLeave={handleDrag}
            onDragOver={handleDrag}
            onDrop={handleDrop}
          >
            <Upload className="mx-auto text-gray-400 mb-4" size={48} />
            <p className="text-lg font-medium text-gray-900 mb-2">
              Drop files here or click to browse
            </p>
            <p className="text-gray-500 mb-4">
              Supports: TXT, JPG, PNG, JSON (max 10MB each)
            </p>
            <button
              onClick={() => fileInputRef.current?.click()}
              className="bg-primary-600 text-white px-6 py-2 rounded-lg hover:bg-primary-700 transition-colors"
            >
              Choose Files
            </button>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              accept=".txt,.jpg,.jpeg,.png,.json"
              onChange={handleFileSelect}
              className="hidden"
            />
          </div>

          {/* Selected Files */}
          {selectedFiles.length > 0 && (
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="font-medium">Selected Files ({selectedFiles.length})</h3>
                <button
                  onClick={uploadAllFiles}
                  disabled={Object.values(uploadStatus).some(status => status === 'uploading')}
                  className="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Upload All
                </button>
              </div>
              
              <div className="max-h-60 overflow-y-auto space-y-2">
                {selectedFiles.map((file, index) => {
                  const fileKey = `${index}-${file.name}`;
                  const progress = uploadProgress[fileKey] || 0;
                  const status = uploadStatus[fileKey];
                  
                  return (
                    <div key={fileKey} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                      {getFileIcon(file.type, 'w-8 h-8')}
                      
                      <div className="flex-1 min-w-0">
                        <p className="font-medium text-gray-900 truncate">{file.name}</p>
                        <p className="text-sm text-gray-500">{formatFileSize(file.size)}</p>
                        
                        {status === 'uploading' && (
                          <div className="mt-2">
                            <div className="flex items-center justify-between text-xs text-gray-600 mb-1">
                              <span>Uploading...</span>
                              <span>{progress}%</span>
                            </div>
                            <div className="w-full bg-gray-200 rounded-full h-1">
                              <div 
                                className="bg-primary-600 h-1 rounded-full transition-all duration-300"
                                style={{ width: `${progress}%` }}
                              />
                            </div>
                          </div>
                        )}
                      </div>
                      
                      <div className="flex items-center gap-2">
                        {status === 'success' && (
                          <CheckCircle className="text-green-600" size={20} />
                        )}
                        {status === 'error' && (
                          <AlertCircle className="text-red-600" size={20} />
                        )}
                        {!status && (
                          <button
                            onClick={() => removeFile(index)}
                            className="text-gray-400 hover:text-red-600 transition-colors"
                          >
                            <X size={20} />
                          </button>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default FileUpload;