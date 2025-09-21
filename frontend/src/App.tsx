import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Layout } from './components/Layout';
import { Dashboard } from './pages/Dashboard';
import { SearchPage } from './pages/SearchPage';
import { MappingPage } from './pages/MappingPage';
import { FhirPage } from './pages/FhirPage';
import { AdminPage } from './pages/AdminPage';
import { NotFound } from './pages/NotFound';

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/mapping" element={<MappingPage />} />
        <Route path="/fhir" element={<FhirPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Layout>
  );
}

export default App;
