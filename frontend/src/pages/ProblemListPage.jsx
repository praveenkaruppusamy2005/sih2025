import React, { useState, useEffect } from 'react';
import { 
  Plus, 
  Search, 
  FileText, 
  User, 
  Calendar,
  Save,
  Eye,
  Edit,
  Trash2,
  CheckCircle,
  AlertCircle,
  Clock,
  Download,
  Upload
} from 'lucide-react';
import apiService from '../services/api';
import toast from 'react-hot-toast';

const ProblemListPage = () => {
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [conditions, setConditions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [patientId, setPatientId] = useState('');
  const [selectedCondition, setSelectedCondition] = useState(null);

  const [formData, setFormData] = useState({
    namasteCode: '',
    patientId: '',
    clinicalStatus: 'active',
    verificationStatus: 'confirmed',
    onsetDate: '',
    notes: ''
  });

  const [autocompleteResults, setAutocompleteResults] = useState([]);
  const [showAutocomplete, setShowAutocomplete] = useState(false);

  useEffect(() => {
    loadConditions();
  }, []);

  const loadConditions = async () => {
    setLoading(true);
    try {
      // In a real implementation, this would load from a database
      setConditions([]);
    } catch (error) {
      toast.error('Failed to load conditions: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleAutocomplete = async (term) => {
    if (term.length < 2) {
      setAutocompleteResults([]);
      setShowAutocomplete(false);
      return;
    }

    try {
      const response = await apiService.getAutoCompleteSuggestions(term, 10);
      setAutocompleteResults(response.expansion?.contains || []);
      setShowAutocomplete(true);
    } catch (error) {
      console.error('Autocomplete error:', error);
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.namasteCode || !formData.patientId) {
      toast.error('Please fill in all required fields');
      return;
    }

    setLoading(true);
    try {
      const response = await apiService.createCondition(formData);
      toast.success('Condition created successfully');
      setShowCreateForm(false);
      setFormData({
        namasteCode: '',
        patientId: '',
        clinicalStatus: 'active',
        verificationStatus: 'confirmed',
        onsetDate: '',
        notes: ''
      });
      loadConditions();
    } catch (error) {
      toast.error('Failed to create condition: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    if (field === 'namasteCode') {
      handleAutocomplete(value);
    }
  };

  const selectAutocompleteItem = (item) => {
    setFormData(prev => ({ ...prev, namasteCode: item.code }));
    setShowAutocomplete(false);
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'active': return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'inactive': return <Clock className="h-4 w-4 text-yellow-500" />;
      case 'resolved': return <CheckCircle className="h-4 w-4 text-blue-500" />;
      default: return <AlertCircle className="h-4 w-4 text-gray-500" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'active': return 'bg-green-100 text-green-800';
      case 'inactive': return 'bg-yellow-100 text-yellow-800';
      case 'resolved': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const ConditionCard = ({ condition }) => (
    <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center space-x-2 mb-2">
            <span className="text-sm font-medium text-gray-900">{condition.namasteCode}</span>
            <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(condition.clinicalStatus)}`}>
              {getStatusIcon(condition.clinicalStatus)}
              <span className="ml-1 capitalize">{condition.clinicalStatus}</span>
            </span>
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-1">{condition.display}</h3>
          <p className="text-sm text-gray-600 mb-2">Patient: {condition.patientId}</p>
          {condition.notes && (
            <p className="text-sm text-gray-600 mb-2">{condition.notes}</p>
          )}
          <div className="flex items-center space-x-4 text-xs text-gray-500">
            <span>Created: {new Date(condition.recordedDate).toLocaleDateString()}</span>
            {condition.onsetDate && <span>Onset: {condition.onsetDate}</span>}
          </div>
        </div>
        <div className="flex items-center space-x-2">
          <button className="p-1 text-gray-400 hover:text-gray-600">
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
      
      {/* Dual Coding Display */}
      <div className="mt-4 pt-4 border-t border-gray-200">
        <h4 className="text-sm font-medium text-gray-900 mb-2">Dual Coding</h4>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-orange-50 p-3 rounded-md">
            <div className="text-xs font-medium text-orange-800 mb-1">NAMASTE</div>
            <div className="text-sm text-orange-900">{condition.namasteCode}</div>
            <div className="text-xs text-orange-700">{condition.display}</div>
          </div>
          {condition.tm2Code && (
            <div className="bg-blue-50 p-3 rounded-md">
              <div className="text-xs font-medium text-blue-800 mb-1">ICD-11 TM2</div>
              <div className="text-sm text-blue-900">{condition.tm2Code}</div>
              <div className="text-xs text-blue-700">{condition.tm2Display}</div>
            </div>
          )}
          {condition.biomedicineCode && (
            <div className="bg-purple-50 p-3 rounded-md">
              <div className="text-xs font-medium text-purple-800 mb-1">ICD-11 Biomedicine</div>
              <div className="text-sm text-purple-900">{condition.biomedicineCode}</div>
              <div className="text-xs text-purple-700">{condition.biomedicineDisplay}</div>
            </div>
          )}
        </div>
      </div>
    </div>
  );

  const CreateConditionForm = () => (
    <div className="bg-white shadow rounded-lg p-6">
      <h2 className="text-lg font-medium text-gray-900 mb-4">Create Dual-Coded Condition</h2>
      
      <form onSubmit={handleFormSubmit} className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label htmlFor="patientId" className="block text-sm font-medium text-gray-700 mb-1">
              Patient ID *
            </label>
            <input
              type="text"
              id="patientId"
              value={formData.patientId}
              onChange={(e) => handleInputChange('patientId', e.target.value)}
              placeholder="Enter patient ID"
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              required
            />
          </div>
          
          <div className="relative">
            <label htmlFor="namasteCode" className="block text-sm font-medium text-gray-700 mb-1">
              NAMASTE Code *
            </label>
            <input
              type="text"
              id="namasteCode"
              value={formData.namasteCode}
              onChange={(e) => handleInputChange('namasteCode', e.target.value)}
              placeholder="Search NAMASTE codes..."
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              required
            />
            
            {/* Autocomplete Dropdown */}
            {showAutocomplete && autocompleteResults.length > 0 && (
              <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-auto">
                {autocompleteResults.map((item, index) => (
                  <button
                    key={index}
                    type="button"
                    onClick={() => selectAutocompleteItem(item)}
                    className="w-full px-3 py-2 text-left hover:bg-gray-50 focus:outline-none focus:bg-gray-50"
                  >
                    <div className="text-sm font-medium text-gray-900">{item.code}</div>
                    <div className="text-xs text-gray-600">{item.display}</div>
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label htmlFor="clinicalStatus" className="block text-sm font-medium text-gray-700 mb-1">
              Clinical Status
            </label>
            <select
              id="clinicalStatus"
              value={formData.clinicalStatus}
              onChange={(e) => handleInputChange('clinicalStatus', e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="active">Active</option>
              <option value="inactive">Inactive</option>
              <option value="resolved">Resolved</option>
            </select>
          </div>
          
          <div>
            <label htmlFor="verificationStatus" className="block text-sm font-medium text-gray-700 mb-1">
              Verification Status
            </label>
            <select
              id="verificationStatus"
              value={formData.verificationStatus}
              onChange={(e) => handleInputChange('verificationStatus', e.target.value)}
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="provisional">Provisional</option>
              <option value="differential">Differential</option>
              <option value="confirmed">Confirmed</option>
              <option value="refuted">Refuted</option>
              <option value="entered-in-error">Entered in Error</option>
              <option value="unknown">Unknown</option>
            </select>
          </div>
        </div>

        <div>
          <label htmlFor="onsetDate" className="block text-sm font-medium text-gray-700 mb-1">
            Onset Date
          </label>
          <input
            type="date"
            id="onsetDate"
            value={formData.onsetDate}
            onChange={(e) => handleInputChange('onsetDate', e.target.value)}
            className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <div>
          <label htmlFor="notes" className="block text-sm font-medium text-gray-700 mb-1">
            Notes
          </label>
          <textarea
            id="notes"
            value={formData.notes}
            onChange={(e) => handleInputChange('notes', e.target.value)}
            rows={3}
            placeholder="Additional notes about the condition..."
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
            {loading ? 'Creating...' : 'Create Condition'}
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
            <h1 className="text-2xl font-bold text-gray-900">Problem List</h1>
            <p className="text-gray-600 mt-1">
              Manage dual-coded conditions with NAMASTE and ICD-11 terminologies
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
              Create Condition
            </button>
          </div>
        </div>
      </div>

      {/* Create Form */}
      {showCreateForm && <CreateConditionForm />}

      {/* Search and Filters */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1">
            <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-1">
              Search Conditions
            </label>
            <div className="relative">
              <input
                type="text"
                id="search"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search by patient ID, code, or notes..."
                className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              />
              <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
            </div>
          </div>
          <div className="sm:w-48">
            <label htmlFor="patientFilter" className="block text-sm font-medium text-gray-700 mb-1">
              Patient ID
            </label>
            <input
              type="text"
              id="patientFilter"
              value={patientId}
              onChange={(e) => setPatientId(e.target.value)}
              placeholder="Filter by patient..."
              className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
        </div>
      </div>

      {/* Conditions List */}
      <div className="space-y-4">
        {loading ? (
          <div className="flex items-center justify-center h-32">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        ) : conditions.length > 0 ? (
          conditions.map((condition) => (
            <ConditionCard key={condition.id} condition={condition} />
          ))
        ) : (
          <div className="bg-white shadow rounded-lg p-6 text-center">
            <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">No conditions found</h3>
            <p className="text-gray-600 mb-4">
              Get started by creating your first dual-coded condition.
            </p>
            <button
              onClick={() => setShowCreateForm(true)}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Plus className="h-4 w-4 mr-2" />
              Create Condition
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export { ProblemListPage };