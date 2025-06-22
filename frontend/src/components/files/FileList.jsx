import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Download, Eye } from 'lucide-react';
import { FileText, Image, Code, File } from 'lucide-react';
import { formatFileSize, formatDate } from '../../utils/fileUtils';
import { fileService } from '../../services/api';

const FileList = ({ files, viewMode }) => {
    console.log("files in file list ", files);
    console.log("viewMode in file list ", viewMode);
  const navigate = useNavigate();

  const handleViewFile = (fileId) => {
    navigate(`/${fileId}/view`);
  };

  const handleDownloadFile = async (fileId, filename) => {
    try {
      const response = await fileService.downloadFile(fileId);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Download failed:', error);
      alert('Download failed. Please try again.');
    }
  };

   const getFileIcon = (mimeType, className = 'w-6 h-6') => {
  if (!mimeType) {
    return <File className={`${className} text-gray-400`} />;
  }
  
  if (mimeType === 'text/plain') {
    return <FileText className={`${className} text-blue-600`} />;
  }
  
  if (mimeType.startsWith('image/')) {
    return <Image className={`${className} text-green-600`} />;
  }
  
  if (mimeType === 'application/json') {
    return <Code className={`${className} text-purple-600`} />;
  }
  
  return <File className={`${className} text-gray-400`} />;
};


  if (viewMode === 'list') {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div className="grid grid-cols-12 gap-4 p-4 bg-gray-50 border-b border-gray-200 text-sm font-medium text-gray-600">
          <div className="col-span-6">Name</div>
          <div className="col-span-2">Size</div>
          <div className="col-span-2">Modified</div>
          <div className="col-span-2">Actions</div>
        </div>
        
        <div className="divide-y divide-gray-200">
          {files.map((file) => (
            <div key={file.id} className="grid grid-cols-12 gap-4 p-4 hover:bg-gray-50 transition-colors">
              <div className="col-span-6 flex items-center gap-3 min-w-0">
                {getFileIcon(file.mimeType, 'w-8 h-8 flex-shrink-0')}
                <div className="min-w-0">
                  <p className="font-medium text-gray-900 truncate">{file.originalFilename}</p>
                  <p className="text-sm text-gray-500">{file.mimeType}</p>
                </div>
              </div>
              
              <div className="col-span-2 flex items-center text-gray-600">
                {formatFileSize(file.fileSize)}
              </div>
              
              <div className="col-span-2 flex items-center text-gray-600">
                {formatDate(file.uploadTimestamp)}
              </div>
              
              <div className="col-span-2 flex items-center gap-2">
                <button
                  onClick={() => handleViewFile(file.id)}
                  className="p-2 text-gray-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                  title="View file"
                >
                  <Eye size={16} />
                </button>
                <button
                  onClick={() => handleDownloadFile(file.id, file.originalFilename)}
                  className="p-2 text-gray-500 hover:text-green-600 hover:bg-green-50 rounded-lg transition-colors"
                  title="Download file"
                >
                  <Download size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

 
  // Grid view
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
      {files.map((file) => (
        <div
          key={file.id}
          className="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow cursor-pointer group"
          onClick={() => handleViewFile(file.id)}
        >
          <div className="p-6">
            {/* File Icon */}
            <div className="flex justify-center mb-4">
              {getFileIcon(file.mimeType, 'w-16 h-16 text-gray-400 group-hover:text-primary-500 transition-colors')}
            </div>
            
            {/* File Info */}
            <div className="text-center space-y-2">
              <h3 className="font-medium text-gray-900 truncate" title={file.originalFilename}>
                {file.originalFilename}
              </h3>
              <p className="text-sm text-gray-500">
                {formatFileSize(file.fileSize)}
              </p>
              <p className="text-xs text-gray-400">
                {formatDate(file.uploadTimestamp)}
              </p>
            </div>
          </div>
          
          {/* Actions */}
          <div className="px-6 py-3 bg-gray-50 border-t border-gray-200 flex justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleViewFile(file.id);
              }}
              className="flex items-center gap-1 px-3 py-1 text-sm text-primary-600 hover:bg-primary-50 rounded-md transition-colors"
            >
              <Eye size={14} />
              View
            </button>
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleDownloadFile(file.id, file.originalFilename);
              }}
              className="flex items-center gap-1 px-3 py-1 text-sm text-green-600 hover:bg-green-50 rounded-md transition-colors"
            >
              <Download size={14} />
              Download
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

export default FileList;