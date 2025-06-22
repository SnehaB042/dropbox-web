import React from 'react';

const LoadingSpinner = ({ className = 'w-8 h-8' }) => {
  return (
    <div className={`${className} animate-spin rounded-full border-2 border-gray-300 border-t-primary-600`} />
  );
};

export default LoadingSpinner;