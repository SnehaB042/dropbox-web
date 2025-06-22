import React, { useState } from 'react';
import { useFiles } from '../../hooks/useFiles';
import FileUpload from '../../components/files/FileUpload.jsx';
import FileList from '../../components/files/FileList.jsx';
import LoadingSpinner from '../../components/generic/LoadingSpinner.jsx';
import { Upload, Grid, List } from 'lucide-react';

const HomePage = () => {
  const [showUpload, setShowUpload] = useState(false);
  const [viewMode, setViewMode] = useState('list'); // 'grid' or 'list'
  const { data: filesData, isLoading, error } = useFiles();

  const files = filesData?.files || [];
  const totalFiles = filesData?.totalFiles || 0;

  console.log("filesData in home page : ", filesData);

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-20">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-20">
        <p className="text-red-600">Error loading files. Please try again.</p>
      </div>
    );
  }

  return (
    <div className="flex-grow space-y-6">
      {/* Action Bar */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <h2 className="text-xl font-semibold text-gray-900">
            My Files ({totalFiles})
          </h2>
        </div>

        <div className="flex items-center gap-3">
          {/* View Mode Toggle */}
          <button
            onClick={() => setViewMode('list')}
            className={`p-2 rounded-md transition-colors ${viewMode === 'list'
                ? 'bg-white text-primary-600 shadow-sm'
                : 'text-gray-500 hover:text-gray-700'
              }`}
          >
            <List size={16} />
          </button>
          <div className="flex items-center bg-gray-100 rounded-lg p-1">
            <button
              onClick={() => setViewMode('grid')}
              className={`p-2 rounded-md transition-colors ${viewMode === 'grid'
                  ? 'bg-white text-primary-600 shadow-sm'
                  : 'text-gray-500 hover:text-gray-700'
                }`}
            >
              <Grid size={16} />
            </button>
          </div>

          {/* Upload Button */}
          <button
            onClick={() => setShowUpload(true)}
            className="flex items-center gap-2 bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors"
          >
            <Upload size={16} />
            Upload File
          </button>
        </div>
      </div>

      {/* Upload Modal */}
      {showUpload && (
        <FileUpload onClose={() => setShowUpload(false)} />
      )}

      {/* Files Content */}
      {files.length === 0 ? (
        <div className="text-center py-20">
          <Upload className="mx-auto text-gray-300 mb-4" size={64} />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No files yet</h3>
          <p className="text-gray-500 mb-6">Upload your first file to get started</p>
          <button
            onClick={() => setShowUpload(true)}
            className="bg-primary-600 text-white px-6 py-3 rounded-lg hover:bg-primary-700 transition-colors"
          >
            Upload File
          </button>
        </div>
      ) : (
        <FileList files={files} viewMode={viewMode} />
      )}
    </div>
  );
};

export default HomePage;