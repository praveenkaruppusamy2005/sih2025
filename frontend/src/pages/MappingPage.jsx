import React, { useState, useEffect } from 'react';
import { 
  Plus, 
  Search, 
  GitBranch, 
  Eye, 
  Edit, 
  Trash2,
  CheckCircle,
  AlertCircle,
  ArrowRight,
  Database,
  FileText,
  Filter,
  Download,
  Upload
} from 'lucide-react';
import apiService from '../services/api';
import { MAPPING_EQUIVALENCE, SYSTEM_COLORS } from '../constants';
import toast from 'react-hot-toast';

const MappingPage = () => {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [mappings, setMappings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedMapping, setSelectedMapping] = useState(null);

  const [formData, setFormData] = useState({
    sourceCode: '',
    sourceSystem: 'http://terminology.ayush.gov.in/CodeSystem/namaste',
    targetCode: '',
    targetSystem: 'http://id.who.int/icd/release/11/2019-04/tm2',
    equivalence: 'EQUIVALENT',
    comment: '',
    confidenceScore: 1.0
  });

  const [sourceSuggestions, setSourceSuggestions] = useState([]);
  const [targetSuggestions, setTargetSuggestions] = useState([]);
  const [showSourceAutocomplete, setShowSourceAutocomplete] = useState(false);
  const [showTargetAutocomplete, setShowTargetAutocomplete] = useState(false);

  useEffect(() => {
    loadMappings();
  }, []);

  const loadMappings = async () => {
    setLoading(true);
    try {
      // In a real implementation, this would load from the API
      setMappings([]);
    } catch (error) {
      toast.error('Failed to load mappings: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSourceAutocomplete = async (term) => {
    if (term.length < 2) {
      setSourceSuggestions([]);
      setShowSourceAutocomplete(false);
      return;
    }

    try {
      const response = await apiService.getNamasteAutoComplete(term, 10);
      setSourceSuggestions(response);
      setShowSourceAutocomplete(true);
    } catch (error) {
      console.error('Source autocomplete error:', error);
    }
  };

  const handleTargetAutocomplete = async (term) => {
    if (term.length < 2) {
      setTargetSuggestions([]);
      setShowTargetAutocomplete(false);
      return;
    }

    try {
      const response = await apiService.getIcd11AutoComplete(term, 10);
      setTargetSuggestions(response);
      setShowTargetAutocomplete(true);
    } catch (error) {
      console.error('Target autocomplete error:', error);
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.sourceCode || !formData.targetCode) {
      toast.error('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      await apiService.createMapping(formData);
      toast.success('Mapping created successfully');
      setShowCreateForm(false);
      setFormData({
        sourceCode: '',
        sourceSystem: 'http://terminology.ayush.gov.in/CodeSystem/namaste',
        targetCode: '',
        targetSystem: 'http://id.who.int/icd/release/11/2019-04/tm2',
        equivalence: 'EQUIVALENT',
        comment: '',
        confidenceScore: 1.0
      });
      loadMappings();
    } catch (error) {
      toast.error('Failed to create mapping: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    if (field === 'sourceCode') {
      handleSourceAutocomplete(value);
    } else if (field === 'targetCode') {
      handleTargetAutocomplete(value);
    }
  };

  const selectSourceItem = (item) => {
    setFormData(prev => ({ ...prev, sourceCode: item.code }));
    setShowSourceAutocomplete(false);
  };

  const selectTargetItem = (item) => {
    setFormData(prev => ({ ...prev, targetCode: item.code }));
    setShowTargetAutocomplete(false);
  };

  const getEquivalenceColor = (equivalence) => {
    switch (equivalence) {
      case 'EQUIVALENT': return 'bg-green-100 text-green-800';
      case 'EQUAL': return 'bg-blue-100 text-blue-800';
      case 'WIDER': return 'bg-yellow-100 text-yellow-800';
      case 'NARROWER': return 'bg-orange-100 text-orange-800';
      case 'RELATEDTO': return 'bg-purple-100 text-purple-800';
      case 'INEXACT': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getSystemIcon = (system) => {
    if (system.includes('namaste')) return <Database className="h-4 w-4" />;
    if (system.includes('icd')) return <FileText className="h-4 w-4" />;
    return <GitBranch className="h-4 w-4" />;
  };

  const MappingCard = ({ mapping }) => (
    <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center space-x-4 mb-3">
            <div className="flex items-center space-x-2">
              {getSystemIcon(mapping.sourceSystem)}
              <span className="text-sm font-medium text-gray-900">{mapping.sourceCode}</span>
            </div>
            <ArrowRight className="h-4 w-4 text-gray-400" />
            <div className="flex items-center space-x-2">
              {getSystemIcon(mapping.targetSystem)}
              <span className="text-sm font-medium text-gray-900">{mapping.targetCode}</span>
            </div>
          </div>
          
          <div className="flex items-center space-x-2 mb-2">
            <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getEquivalenceColor(mapping.equivalence)}`}>
              {mapping.equivalence}
            </span>
            {mapping.confidenceScore && (
              <span className="text-xs text-gray-500">
                Confidence: {(mapping.confidenceScore * 100).toFixed(0)}%
              </span>
            )}
          </div>
          
          {mapping.comment && (
            <p className="text-sm text-gray-600 mb-2">{mapping.comment}</p>
          )}
          
          <div className="flex items-center space-x-4 text-xs text-gray-500">
            <span>Created: {new Date(mapping.createdAt).toLocaleDateString()}</span>
            {mapping.mappingVersion && <span>Version: {mapping.mappingVersion}</span>}
          </div>
        </div>
        
        <div className="flex items-center space-x-2">
          <button 
            onClick={() => setSelectedMapping(mapping)}
            className="p-1 text-gray-400 hover:text-gray-600"
          >
            <Eye className="h-4 w-4" />
          </button>
          <button className="p-1 text-gray-400 hover:text-gray-600">
            <Edit className="h-4 w-4" />
          </button>
          <button className="p-1 text-gray-400 hover:text-red-600">
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  );

  const CreateMappingForm = () => (
    <div className="bg-white shadow rounded-lg p-6">
      <h2 className="text-lg font-medium text-gray-900 mb-4">Create Concept Mapping</h2>
      
      <form onSubmit={handleFormSubmit} className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="relative">
            <label htmlFor="sourceCode" className="block text-sm font-medium text-gray-700 mb-1">
              Source Code *
            </label>
            <input
              type="text"
              id="sourceCode"
              value={formData.sourceCode}
              onChange={(e) => handleInputChange('sourceCode', e.target.value)}
              placeholder="Search source codes..."
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              required
            />
            
            {showSourceAutocomplete && sourceSuggestions.length > 0 && (
              <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-auto">
                {sourceSuggestions.map((item, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => selectSourceItem(item)}
                    className="w-full px-3 py-2 text-left hover:bg-gray-50 focus:outline-none focus:bg-gray-50"
                  >
                    <div className="text-sm font-medium text-gray-900">{item.code}</div>
                    <div className="text-xs text-gray-600">{item.display || item.title}</div>
                  </button>
                ))}
              </div>
            )}
          </div>
          
          <div className="relative">
            <label htmlFor="targetCode" className="block text-sm font-medium text-gray-700 mb-1">
              Target Code *
            </label>
            <input
              type="text"
              id="targetCode"
              value={formData.targetCode}
              onChange={(e) => handleInputChange('targetCode', e.target.value)}
              placeholder="Search target codes..."
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              required
            />
            
            {showTargetAutocomplete && targetSuggestions.length > 0 && (
              <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-auto">
                {targetSuggestions.map((item, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => selectTargetItem(item)}
                    className="w-full px-3 py-2 text-left hover:bg-gray-50 focus:outline-none focus:bg-gray-50"
                  >
                    <div className="text-sm font-medium text-gray-900">{item.code}</div>
                    <div className="text-xs text-gray-600">{item.title || item.display}</div>
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label htmlFor="sourceSystem" className="block text-sm font-medium text-gray-700 mb-1">
              Source System
            </label>
            <select
              id="sourceSystem"
              value={formData.sourceSystem}
              onChange={(e) => handleInputChange('sourceSystem', e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="http://terminology.ayush.gov.in/CodeSystem/namaste">NAMASTE</option>
            </select>
          </div>
          
          <div>
            <label htmlFor="targetSystem" className="block text-sm font-medium text-gray-700 mb-1">
              Target System
            </label>
            <select
              id="targetSystem"
              value={formData.targetSystem}
              onChange={(e) => handleInputChange('targetSystem', e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="http://id.who.int/icd/release/11/2019-04/tm2">ICD-11 TM2</option>
              <option value="http://id.who.int/icd/release/11/2019-04">ICD-11 Biomedicine</option>
            </select>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label htmlFor="equivalence" className="block text-sm font-medium text-gray-700 mb-1">
              Equivalence
            </label>
            <select
              id="equivalence"
              value={formData.equivalence}
              onChange={(e) => handleInputChange('equivalence', e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            >
              {Object.entries(MAPPING_EQUIVALENCE).map(([key, value]) => (
                <option key={key} value={value}>{value}</option>
              ))}
            </select>
          </div>
          
          <div>
            <label htmlFor="confidenceScore" className="block text-sm font-medium text-gray-700 mb-1">
              Confidence Score
            </label>
            <input
              type="number"
              id="confidenceScore"
              min="0"
              max="1"
              step="0.1"
              value={formData.confidenceScore}
              onChange={(e) => handleInputChange('confidenceScore', parseFloat(e.target.value))}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
        </div>

        <div>
          <label htmlFor="comment" className="block text-sm font-medium text-gray-700 mb-1">
            Comment
          </label>
          <textarea
            id="comment"
            value={formData.comment}
            onChange={(e) => handleInputChange('comment', e.target.value)}
            rows={3}
            placeholder="Additional notes about this mapping..."
            className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <div className="flex justify-end space-x-3">
          <button
            type="button"
            onClick={() => setShowCreateForm(false)}
            className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-4 py-2 bg-blue-600 text-white rounded-md text-sm font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Creating...' : 'Create Mapping'}
          </button>
        </div>
      </form>
    </div>
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Concept Mappings</h1>
            <p className="text-gray-600 mt-1">
              Manage mappings between NAMASTE and ICD-11 terminology systems
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <button className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <Upload className="h-4 w-4 mr-2" />
              Import
            </button>
            <button className="inline-flex items-center px-3 py-2 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              <Download className="h-4 w-4 mr-2" />
              Export
            </button>
            <button
              onClick={() => setShowCreateForm(true)}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Plus className="h-4 w-4 mr-2" />
              Create Mapping
            </button>
          </div>
        </div>
      </div>

      {/* Create Form */}
      {showCreateForm && <CreateMappingForm />}

      {/* Search and Filters */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1">
            <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-1">
              Search Mappings
            </label>
            <div className="relative">
              <input
                type="text"
                id="search"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search by source code, target code, or comment..."
                className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              />
              <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
            </div>
          </div>
        </div>
      </div>

      {/* Mappings List */}
      <div className="space-y-4">
        {loading ? (
          <div className="flex items-center justify-center h-32">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        ) : mappings.length > 0 ? (
          mappings.map((mapping) => (
            <MappingCard key={mapping.id} mapping={mapping} />
          ))
        ) : (
          <div className="bg-white shadow rounded-lg p-6 text-center">
            <GitBranch className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No mappings found</h3>
            <p className="text-gray-600 mb-4">
              Get started by creating your first concept mapping.
            </p>
            <button
              onClick={() => setShowCreateForm(true)}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Plus className="h-4 w-4 mr-2" />
              Create Mapping
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export { MappingPage };