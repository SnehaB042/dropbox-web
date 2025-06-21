import React from 'react';
import UploadForm from '../components/UploadForm';
import FileList from '../components/FileList';

const Home = () => {
  return (
    <div className="p-4">
      <h1 className="text-2xl font-bold mb-4">Dropbox Clone</h1>
      <UploadForm />
      <FileList />
    </div>
  );
};

export default Home;
