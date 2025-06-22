import React from 'react';
import Header from './Header';

const Layout = ({ children }) => {
  return (
    <div className="min-h-screen w-full flex flex-col bg-gray-50"> 
      <Header />
      <main className="flex-grow w-full px-4 py-6">
        {children}
      </main>
    </div>
  );
};

export default Layout;