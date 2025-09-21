import React, { useState, useEffect } from 'react';
import { 
  Search, 
  Filter, 
  Download, 
  Eye, 
  GitBranch,
  FileText,
  Database,
  ChevronDown,
  ChevronUp,
  Copy,
  Check
} from 'lucide-react';
import apiService from '../services/api';
import { SYSTEM_COLORS, TRADITIONAL_SYSTEMS, CODE_TYPES } from '../constants';
import toast from 'react-hot-toast';

const SearchPage = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState('all'); // all, namaste, icd11
  const [systemFilter, setSystemFilter] = useState('');
  const [codeTypeFilter, setCodeTypeFilter] = useState('');
  const [results, setResults] = useState({ namaste: [], icd11: [] });
  const [loading, setLoading] = useState(false);
  const [expandedItems, setExpandedItems] = useState(new Set());
  const [copiedItems, setCopiedItems] = useState(new Set());

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      toast.error('Please enter a search term');
      return;
    }

    setLoading(true);
    try {
      const promises = [];
      
      if (searchType === 'all' || searchType === 'namaste') {
        promises.push(apiService.searchNamasteCodes(searchTerm, 0, 20));
      }
      
      if (searchType === 'all' || searchType === 'icd11') {
        promises.push(apiService.searchIcd11Codes(searchTerm, 0, 20));
      }

      const responses = await Promise.all(promises);
      
      if (searchType === 'all') {
        setResults({
          namaste: responses[0]?.content || [],
          icd11: responses[1]?.content || []
        });
      } else if (searchType === 'namaste') {
        setResults({
          namaste: responses[0]?.content || [],
          icd11: []
        });
      } else {
        setResults({
          namaste: [],
          icd11: responses[0]?.content || []
        });
      }
    } catch (error) {
      toast.error('Search failed: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const toggleExpanded = (itemId) => {
    const newExpanded = new Set(expandedItems);
    if (newExpanded.has(itemId)) {
      newExpanded.delete(itemId);
    } else {
      newExpanded.add(itemId);
    }
    setExpandedItems(newExpanded);
  };

  const copyToClipboard = async (text, itemId) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopiedItems(prev => new Set([...prev, itemId]));
      toast.success('Copied to clipboard');
      setTimeout(() => {
        setCopiedItems(prev => {
          const newSet = new Set(prev);
          newSet.delete(itemId);
          return newSet;
        });
      }, 2000);
    } catch (error) {
      toast.error('Failed to copy to clipboard');
    }
  };

  const getSystemColor = (system) => {
    switch (system) {
      case 'AYURVEDA': return SYSTEM_COLORS.AYURVEDA.primary;
      case 'SIDDHA': return SYSTEM_COLORS.SIDDHA.primary;
      case 'UNANI': return SYSTEM_COLORS.UNANI.primary;
      case 'TM2': return SYSTEM_COLORS.TM2.primary;
      case 'BIOMEDICINE': return SYSTEM_COLORS.BIOMEDICINE.primary;
      default: return '#6B7280';
    }
  };

  const NamasteResultCard = ({ item }) => {
    const isExpanded = expandedItems.has(`namaste-${item.id}`);
    const isCopied = copiedItems.has(`namaste-${item.id}`);

    return (
      <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center space-x-2 mb-2">
              <span className="text-sm font-medium text-gray-900">{item.code}</span>
              <span 
                className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium text-white"
                style={{ backgroundColor: getSystemColor(item.system) }}
              >
                {item.system}
              </span>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-1">{item.display}</h3>
            {item.definition && (
              <p className="text-sm text-gray-600 mb-2">{item.definition}</p>
            )}
            <div className="flex items-center space-x-4 text-xs text-gray-500">
              {item.category && <span>Category: {item.category}</span>}
              {item.whoTerminologyCode && <span>WHO: {item.whoTerminologyCode}</span>}
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => copyToClipboard(item.code, `namaste-${item.id}`)}
              className="p-1 text-gray-400 hover:text-gray-600"
            >
              {isCopied ? <Check className="h-4 w-4 text-green-500" /> : <Copy className="h-4 w-4" />}
            </button>
            <button
              onClick={() => toggleExpanded(`namaste-${item.id}`)}
              className="p-1 text-gray-400 hover:text-gray-600"
            >
              {isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
            </button>
          </div>
        </div>
        
        {isExpanded && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <h4 className="text-sm font-medium text-gray-900 mb-2">Additional Information</h4>
                <dl className="space-y-1">
                  <div className="flex">
                    <dt className="text-sm text-gray-500 w-20">System:</dt>
                    <dd className="text-sm text-gray-900">{item.system}</dd>
                  </div>
                  {item.category && (
                    <div className="flex">
                      <dt className="text-sm text-gray-500 w-20">Category:</dt>
                      <dd className="text-sm text-gray-900">{item.category}</dd>
                    </div>
                  )}
                  {item.subcategory && (
                    <div className="flex">
                      <dt className="text-sm text-gray-500 w-20">Subcategory:</dt>
                      <dd className="text-sm text-gray-900">{item.subcategory}</dd>
                    </div>
                  )}
                </dl>
              </div>
              <div>
                <h4 className="text-sm font-medium text-gray-900 mb-2">Mappings</h4>
                <dl className="space-y-1">
                  {item.whoTerminologyCode && (
                    <div className="flex">
                      <dt className="text-sm text-gray-500 w-20">WHO:</dt>
                      <dd className="text-sm text-gray-900">{item.whoTerminologyCode}</dd>
                    </div>
                  )}
                  {item.icd11Tm2Code && (
                    <div className="flex">
                      <dt className="text-sm text-gray-500 w-20">TM2:</dt>
                      <dd className="text-sm text-gray-900">{item.icd11Tm2Code}</dd>
                    </div>
                  )}
                  {item.icd11BiomedicineCode && (
                    <div className="flex">
                      <dt className="text-sm text-gray-500 w-20">Biomed:</dt>
                      <dd className="text-sm text-gray-900">{item.icd11BiomedicineCode}</dd>
                    </div>
                  )}
                </dl>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  };

  const Icd11ResultCard = ({ item }) => {
    const isExpanded = expandedItems.has(`icd11-${item.id}`);
    const isCopied = copiedItems.has(`icd11-${item.id}`);

    return (
      <div className="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center space-x-2 mb-2">
              <span className="text-sm font-medium text-gray-900">{item.code}</span>
              <span 
                className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium text-white"
                style={{ backgroundColor: getSystemColor(item.codeType) }}
              >
                {item.codeType}
              </span>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-1">{item.title}</h3>
            {item.definition && (
              <p className="text-sm text-gray-600 mb-2">{item.definition}</p>
            )}
            <div className="flex items-center space-x-4 text-xs text-gray-500">
              {item.chapter && <span>Chapter: {item.chapter}</span>}
              {item.parent && <span>Parent: {item.parent}</span>}
            </div>
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => copyToClipboard(item.code, `icd11-${item.id}`)}
              className="p-1 text-gray-400 hover:text-gray-600"
            >
              {isCopied ? <Check className="h-4 w-4 text-green-500" /> : <Copy className="h-4 w-4" />}
            </button>
            <button
              onClick={() => toggleExpanded(`icd11-${item.id}`)}
              className="p-1 text-gray-400 hover:text-gray-600"
            >
              {isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
            </button>
          </div>
        </div>
        
        {isExpanded && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <h4 className="text-sm font-medium text-gray-900 mb-2">Code Information</h4>
                <dl className="space-y-1">
                  <div className="flex">
                    <dt className="text-sm text-gray-500 w-20">Type:</dt>
                    <dd className="text-sm text-gray-900">{item.codeType}</dd>
                  </div>
                  {item.chapter && (
                    <div className="flex">
                      <dt className="text-sm text-gray-500 w-20">Chapter:</dt>
                      <dd className="text-sm text-gray-900">{item.chapter}</dd>
                    </div>
                  )}
                  {item.parent && (
                    <div className="flex">
                      <dt className="text-sm text-gray-500 w-20">Parent:</dt>
                      <dd className="text-sm text-gray-900">{item.parent}</dd>
                    </div>
                  )}
                </dl>
              </div>
              <div>
                <h4 className="text-sm font-medium text-gray-900 mb-2">Additional Details</h4>
                <dl className="space-y-1">
                  {item.foundationUri && (
                    <div className="flex">
                      <dt className="text-sm text-gray-500 w-20">Foundation:</dt>
                      <dd className="text-sm text-gray-900 truncate">{item.foundationUri}</dd>
                    </div>
                  )}
                  {item.linearizationUri && (
                    <div className="flex">
                      <dt className="text-sm text-gray-500 w-20">Linearization:</dt>
                      <dd className="text-sm text-gray-900 truncate">{item.linearizationUri}</dd>
                    </div>
                  )}
                </dl>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Search Terminology Codes</h1>
        <p className="text-gray-600">
          Search across NAMASTE and ICD-11 terminology systems
        </p>
      </div>

      {/* Search Form */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-1">
                Search Term
              </label>
              <div className="relative">
                <input
                  type="text"
                  id="search"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="Enter code, display name, or definition..."
                  className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                />
                <Search className="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
              </div>
            </div>
            <div className="sm:w-48">
              <label htmlFor="searchType" className="block text-sm font-medium text-gray-700 mb-1">
                Search Type
              </label>
              <select
                id="searchType"
                value={searchType}
                onChange={(e) => setSearchType(e.target.value)}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="all">All Systems</option>
                <option value="namaste">NAMASTE Only</option>
                <option value="icd11">ICD-11 Only</option>
              </select>
            </div>
            <div className="sm:w-32">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                &nbsp;
              </label>
              <button
                onClick={handleSearch}
                disabled={loading}
                className="w-full bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Searching...' : 'Search'}
              </button>
            </div>
          </div>

          {/* Filters */}
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="sm:w-48">
              <label htmlFor="systemFilter" className="block text-sm font-medium text-gray-700 mb-1">
                NAMASTE System
              </label>
              <select
                id="systemFilter"
                value={systemFilter}
                onChange={(e) => setSystemFilter(e.target.value)}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">All Systems</option>
                <option value="AYURVEDA">Ayurveda</option>
                <option value="SIDDHA">Siddha</option>
                <option value="UNANI">Unani</option>
              </select>
            </div>
            <div className="sm:w-48">
              <label htmlFor="codeTypeFilter" className="block text-sm font-medium text-gray-700 mb-1">
                ICD-11 Type
              </label>
              <select
                id="codeTypeFilter"
                value={codeTypeFilter}
                onChange={(e) => setCodeTypeFilter(e.target.value)}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">All Types</option>
                <option value="TM2">TM2 (Traditional Medicine)</option>
                <option value="BIOMEDICINE">Biomedicine</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Results */}
      {(results.namaste.length > 0 || results.icd11.length > 0) && (
        <div className="space-y-6">
          {/* NAMASTE Results */}
          {results.namaste.length > 0 && (
            <div>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-medium text-gray-900 flex items-center">
                  <Database className="h-5 w-5 mr-2 text-orange-600" />
                  NAMASTE Results ({results.namaste.length})
                </h2>
              </div>
              <div className="space-y-4">
                {results.namaste.map((item) => (
                  <NamasteResultCard key={item.id} item={item} />
                ))}
              </div>
            </div>
          )}

          {/* ICD-11 Results */}
          {results.icd11.length > 0 && (
            <div>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg font-medium text-gray-900 flex items-center">
                  <FileText className="h-5 w-5 mr-2 text-blue-600" />
                  ICD-11 Results ({results.icd11.length})
                </h2>
              </div>
              <div className="space-y-4">
                {results.icd11.map((item) => (
                  <Icd11ResultCard key={item.id} item={item} />
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* No Results */}
      {!loading && searchTerm && results.namaste.length === 0 && results.icd11.length === 0 && (
        <div className="bg-white shadow rounded-lg p-6 text-center">
          <Search className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No results found</h3>
          <p className="text-gray-600">
            Try adjusting your search terms or filters to find what you're looking for.
          </p>
        </div>
      )}
    </div>
  );
};

export { SearchPage };