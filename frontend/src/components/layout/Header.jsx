import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Cloud, ArrowLeft } from 'lucide-react';

const Header = () => {
  const location = useLocation();
  const isFileViewer = location.pathname.includes('/view');

  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="mx-auto px-4 py-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            {isFileViewer ? (
              <Link 
                to="/" 
                className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors"
              >
                <ArrowLeft size={20} />
                <span>Back to Files</span>
              </Link>
            ) : (
              <Link to="/" className="flex items-center gap-2">
                <Cloud className="text-primary-600" size={32} />
                <h1 className="text-2xl font-bold text-gray-900">DropBox Application</h1>
              </Link>
            )}
          </div>
          
          {!isFileViewer && (
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-500">
                Simple file storage
              </span>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;