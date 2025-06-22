import React from 'react';
import { Link } from 'react-router-dom';
import { Home, FileX } from 'lucide-react';

const NotFoundPage = () => {
  return (
    <div className="text-center py-20">
      <FileX className="mx-auto text-gray-300 mb-6" size={64} />
      <h1 className="text-3xl font-bold text-gray-900 mb-4">Page Not Found</h1>
      <p className="text-gray-600 mb-8">
        The page you're looking for doesn't exist or has been moved.
      </p>
      <Link
        to="/"
        className="inline-flex items-center gap-2 bg-primary-600 text-white px-6 py-3 rounded-lg hover:bg-primary-700 transition-colors"
      >
        <Home size={20} />
        Back to Home
      </Link>
    </div>
  );
};

export default NotFoundPage;