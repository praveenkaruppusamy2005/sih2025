import React, { useState, useEffect } from 'react';
import { 
  FileText, 
  Download, 
  Eye, 
  Copy, 
  Check,
  RefreshCw,
  Code,
  Database,
  GitBranch,
  Settings,
  AlertCircle,
  CheckCircle
} from 'lucide-react';
import apiService from '../services/api';
import toast from 'react-hot-toast';

const FhirPage = () => {
  const [activeTab, setActiveTab] = useState('metadata');
  const [resources, setResources] = useState({
    metadata: null,
    codeSystem: null,
    conceptMap: null,
    valueSet: null
  });
  const [loading, setLoading] = useState(false);
  const [format, setFormat] = useState('json');
  const [copiedItems, setCopiedItems] = useState(new Set());

  const tabs = [
    { id: 'metadata', name: 'Capability Statement', icon: Settings },
    { id: 'codeSystem', name: 'NAMASTE CodeSystem', icon: Database },
    { id: 'conceptMap', name: 'ConceptMap', icon: GitBranch },
    { id: 'valueSet', name: 'ValueSet', icon: FileText }
  ];

  useEffect(() => {
    loadResource(activeTab);
  }, [activeTab, format]);

  const loadResource = async (resourceType) => {
    setLoading(true);
    try {
      let response;
      switch (resourceType) {
        case 'metadata':
          response = await apiService.getFhirMetadata(format);
          break;
        case 'codeSystem':
          response = await apiService.getNamasteCodeSystem(format);
          break;
        case 'conceptMap':
          response = await apiService.getNamasteToIcd11ConceptMap(format);
          break;
        case 'valueSet':
          response = await apiService.getNamasteValueSet(null, null, format);
          break;
        default:
          return;
      }
      
      setResources(prev => ({
        ...prev,
        [resourceType]: response
      }));
    } catch (error) {
      toast.error(`Failed to load ${resourceType}: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = async (content, itemId) => {
    try {
      await navigator.clipboard.writeText(content);
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

  const downloadResource = (content, filename) => {
    const blob = new Blob([content], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const formatJson = (json) => {
    try {
      return JSON.stringify(json, null, 2);
    } catch (error) {
      return json;
    }
  };

  const ResourceViewer = ({ resource, resourceType }) => {
    if (!resource) {
      return (
        <div className="flex items-center justify-center h-64">
          <div className="text-center">
            <AlertCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-600">No resource loaded</p>
          </div>
        </div>
      );
    }

    const content = typeof resource === 'string' ? resource : formatJson(resource);
    const filename = `${resourceType}-${format}.${format === 'json' ? 'json' : 'xml'}`;

    return (
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <CheckCircle className="h-5 w-5 text-green-500" />
            <span className="text-sm text-gray-600">Resource loaded successfully</span>
          </div>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => copyToClipboard(content, resourceType)}
              className="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              {copiedItems.has(resourceType) ? <Check className="h-4 w-4 mr-1" /> : <Copy className="h-4 w-4 mr-1" />}
              Copy
            </button>
            <button
              onClick={() => downloadResource(content, filename)}
              className="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              <Download className="h-4 w-4 mr-1" />
              Download
            </button>
          </div>
        </div>
        
        <div className="bg-gray-900 rounded-lg p-4 overflow-auto">
          <pre className="text-sm text-gray-100 whitespace-pre-wrap">
            {content}
          </pre>
        </div>
      </div>
    );
  };

  const ResourceInfo = ({ resourceType }) => {
    const info = {
      metadata: {
        title: 'FHIR Capability Statement',
        description: 'Describes the capabilities of this FHIR terminology server',
        url: '/fhir/metadata'
      },
      codeSystem: {
        title: 'NAMASTE CodeSystem',
        description: 'Complete NAMASTE terminology codes with properties and relationships',
        url: '/fhir/CodeSystem/namaste-codes'
      },
      conceptMap: {
        title: 'NAMASTE to ICD-11 ConceptMap',
        description: 'Mappings between NAMASTE codes and ICD-11 TM2/Biomedicine codes',
        url: '/fhir/ConceptMap/namaste-to-icd11'
      },
      valueSet: {
        title: 'NAMASTE ValueSet',
        description: 'Filtered collection of NAMASTE codes for specific use cases',
        url: '/fhir/ValueSet/namaste'
      }
    };

    const currentInfo = info[resourceType];

    return (
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="text-lg font-medium text-blue-900 mb-2">{currentInfo.title}</h3>
        <p className="text-blue-800 mb-3">{currentInfo.description}</p>
        <div className="flex items-center space-x-2 text-sm text-blue-700">
          <Code className="h-4 w-4" />
          <span>Endpoint: {currentInfo.url}</span>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">FHIR Resources</h1>
            <p className="text-gray-600 mt-1">
              View and download FHIR R4 compliant terminology resources
            </p>
          </div>
          <div className="flex items-center space-x-3">
            <div className="flex items-center space-x-2">
              <label htmlFor="format" className="text-sm font-medium text-gray-700">
                Format:
              </label>
              <select
                id="format"
                value={format}
                onChange={(e) => setFormat(e.target.value)}
                className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="json">JSON</option>
                <option value="xml">XML</option>
              </select>
            </div>
            <button
              onClick={() => loadResource(activeTab)}
              disabled={loading}
              className="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
            >
              <RefreshCw className={`h-4 w-4 mr-1 ${loading ? 'animate-spin' : ''}`} />
              Refresh
            </button>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white shadow rounded-lg">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8 px-6">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`py-4 px-1 border-b-2 font-medium text-sm flex items-center space-x-2 ${
                    activeTab === tab.id
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <Icon className="h-4 w-4" />
                  <span>{tab.name}</span>
                </button>
              );
            })}
          </nav>
        </div>

        <div className="p-6">
          {/* Resource Info */}
          <ResourceInfo resourceType={activeTab} />

          {/* Resource Content */}
          <div className="mt-6">
            {loading ? (
              <div className="flex items-center justify-center h-64">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              </div>
            ) : (
              <ResourceViewer 
                resource={resources[activeTab]} 
                resourceType={activeTab}
              />
            )}
          </div>
        </div>
      </div>

      {/* FHIR Compliance Info */}
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">FHIR R4 Compliance</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h4 className="text-sm font-medium text-gray-900 mb-2">Supported Resources</h4>
            <ul className="space-y-1 text-sm text-gray-600">
              <li>• CodeSystem (NAMASTE terminology)</li>
              <li>• ConceptMap (NAMASTE ↔ ICD-11)</li>
              <li>• ValueSet (Filtered code collections)</li>
              <li>• CapabilityStatement (Server capabilities)</li>
              <li>• Parameters (Translation operations)</li>
            </ul>
          </div>
          <div>
            <h4 className="text-sm font-medium text-gray-900 mb-2">Supported Operations</h4>
            <ul className="space-y-1 text-sm text-gray-600">
              <li>• $translate (ConceptMap translation)</li>
              <li>• $expand (ValueSet expansion)</li>
              <li>• $validate-code (Code validation)</li>
              <li>• $lookup (Code lookup)</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export { FhirPage };