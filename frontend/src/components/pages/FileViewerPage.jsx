import React from 'react';
import { useParams } from 'react-router-dom';
import { useFileMetadata, useFileContent } from '../../hooks/useFiles';
import { Download, FileText, Image, Code } from 'lucide-react';
import { formatFileSize, formatDate } from '../../utils/fileUtils';
import { fileService } from '../../services/api';
import LoadingSpinner from '../generic/LoadingSpinner';

const FileViewerPage = () => {
  const { fileId } = useParams();
  const { data: file, isLoading: loadingMetadata, error: metadataError } = useFileMetadata(fileId);
  const { data: content, isLoading: loadingContent } = useFileContent(fileId, file?.mimeType);

  console.log("content in file viewer : ", content);

  const handleDownload = async () => {
    if (!file) return;
    
    try {
      const response = await fileService.downloadFile(file.id);
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', file.originalFilename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Download failed:', error);
      alert('Download failed. Please try again.');
    }
  };

  if (loadingMetadata) {
    return (
      <div className="flex justify-center items-center py-20">
        <LoadingSpinner />
      </div>
    );
  }

  if (metadataError || !file) {
    return (
      <div className="text-center py-20">
        <p className="text-red-600">Error loading file. Please try again.</p>
      </div>
    );
  }

  const renderFileContent = () => {
    if (loadingContent) {
      return (
        <div className="flex justify-center items-center py-20">
          <LoadingSpinner />
        </div>
      );
    }

    const contentType = file.mimeType;
    console.log("file in file viewer : ", file);

    // Text files
    if (contentType === 'text/plain') {
      return (
        <div className="bg-gray-50 rounded-lg p-6 border">
          <div className="flex items-center gap-2 mb-4">
            <FileText className="text-blue-600" size={20} />
            <span className="font-medium">Text Content</span>
          </div>
          <pre className="whitespace-pre-wrap text-sm text-gray-800 font-mono bg-white p-4 rounded border overflow-auto max-h-96">
            {content || 'No content available'}
          </pre>
        </div>
      );
    }

    // JSON files
    if (contentType === 'application/json') {
      let formattedJson = content;
      try {
        const parsed = JSON.parse(content);
        formattedJson = JSON.stringify(parsed, null, 2);
      } catch (e) {
        // Keep original content if parsing fails
        console.log("error occured : " + e);
      }

      return (
        <div className="bg-gray-50 rounded-lg p-6 border">
          <div className="flex items-center gap-2 mb-4">
            <Code className="text-purple-600" size={20} />
            <span className="font-medium">JSON Content</span>
          </div>
          <pre className="text-sm text-gray-800 font-mono bg-white p-4 rounded border overflow-auto max-h-96">
            {formattedJson || 'No content available'}
          </pre>
        </div>
      );
    }

    // Image files
    if (contentType?.startsWith('image/')) {
      return (
        <div className="bg-gray-50 rounded-lg p-6 border">
          <div className="flex items-center gap-2 mb-4">
            <Image className="text-green-600" size={20} />
            <span className="font-medium">Image Preview</span>
          </div>
          <div className="flex justify-center">
            <img
              src={`/api/${file.id}/download`}
              alt={file.originalFilename}
              className="max-w-full max-h-96 rounded-lg shadow-sm"
              onError={(e) => {
                e.target.style.display = 'none';
                e.target.nextSibling.style.display = 'block';
              }}
            />
            <div className="hidden text-center py-20 text-gray-500">
              <Image size={48} className="mx-auto mb-2 text-gray-300" />
              <p>Unable to display image</p>
            </div>
          </div>
        </div>
      );
    }

    // Unsupported file types
    return (
      <div className="bg-gray-50 rounded-lg p-6 border text-center">
        <FileText className="mx-auto text-gray-300 mb-4" size={48} />
        <p className="text-gray-600 mb-4">
          Preview not available for this file type
        </p>
        <button
          onClick={handleDownload}
          className="bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors"
        >
          Download to View
        </button>
      </div>
    );
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* File Header */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <div className="flex items-start justify-between">
          <div className="flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-gray-900 mb-2 break-words">
              {file.originalFilename}
            </h1>
            <div className="flex flex-wrap items-center gap-4 text-sm text-gray-500">
              <span>Size: {formatFileSize(file.fileSize)}</span>
              <span>Type: {file.contentType}</span>
              <span>Uploaded: {formatDate(file.uploadTimestamp)}</span>
            </div>
          </div>
          
          <button
            onClick={handleDownload}
            className="flex items-center gap-2 bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors ml-4"
          >
            <Download size={16} />
            Download
          </button>
        </div>
      </div>

      {/* File Content */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        {renderFileContent()}
      </div>
    </div>
  );
};

export default FileViewerPage;