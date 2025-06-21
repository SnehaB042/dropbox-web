import React from 'react';

const UploadForm = () => {
  return (
    <form className="mb-4">
      <input type="file" />
      <button type="submit" className="ml-2 bg-blue-500 text-white px-4 py-2 rounded">Upload</button>
    </form>
  );
};

export default UploadForm;
