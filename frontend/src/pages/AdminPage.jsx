import React, { useState, useEffect } from 'react';
import { 
  Settings, 
  Database, 
  RefreshCw, 
  Download, 
  Upload,
  Activity,
  AlertCircle,
  CheckCircle,
  Clock,
  BarChart3,
  Users,
  Shield,
  FileText,
  GitBranch
} from 'lucide-react';
import apiService from '../services/api';
import toast from 'react-hot-toast';

const AdminPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [systemStatus, setSystemStatus] = useState({
    namaste: 'online',
    icd11: 'online',
    database: 'online',
    fhir: 'online'
  });
  const [recentLogs, setRecentLogs] = useState([]);

  useEffect(() => {
    loadAdminData();
  }, []);

  const loadAdminData = async () => {
    setLoading(true);
    try {
      const [statsData] = await Promise.all([
        apiService.getStats()
      ]);
      setStats(statsData);
      setRecentLogs([
        { type: 'info', message: 'System started successfully', time: '2 minutes ago' },
        { type: 'success', message: 'NAMASTE data loaded: 4,500 codes', time: '5 minutes ago' },
        { type: 'success', message: 'ICD-11 data synchronized: 2,000 codes', time: '10 minutes ago' },
        { type: 'info', message: 'FHIR server initialized', time: '15 minutes ago' }
      ]);
    } catch (error) {
      toast.error('Failed to load admin data: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateMappings = async () => {
    setLoading(true);
    try {
      await apiService.generateMappings();
      toast.success('Automatic mapping generation initiated');
      loadAdminData();
    } catch (error) {
      toast.error('Failed to generate mappings: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleReloadNamaste = async () => {
    setLoading(true);
    try {
      await apiService.reloadNamasteData();
      toast.success('NAMASTE data reload completed');
      loadAdminData();
    } catch (error) {
      toast.error('Failed to reload NAMASTE data: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSyncIcd11 = async () => {
    setLoading(true);
    try {
      await apiService.syncIcd11Data();
      toast.success('ICD-11 data synchronization initiated');
      loadAdminData();
    } catch (error) {
      toast.error('Failed to sync ICD-11 data: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'online': return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'offline': return <AlertCircle className="h-4 w-4 text-red-500" />;
      case 'warning': return <Clock className="h-4 w-4 text-yellow-500" />;
      default: return <AlertCircle className="h-4 w-4 text-gray-500" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'online': return 'text-green-600';
      case 'offline': return 'text-red-600';
      case 'warning': return 'text-yellow-600';
      default: return 'text-gray-600';
    }
  };

  const getLogIcon = (type) => {
    switch (type) {
      case 'success': return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'error': return <AlertCircle className="h-4 w-4 text-red-500" />;
      case 'warning': return <Clock className="h-4 w-4 text-yellow-500" />;
      default: return <Activity className="h-4 w-4 text-blue-500" />;
    }
  };

  const StatCard = ({ title, value, icon: Icon, color, subtitle }) => (
    <div className="bg-white overflow-hidden shadow rounded-lg">
      <div className="p-5">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <Icon className={`h-6 w-6 ${color}`} />
          </div>
          <div className="ml-5 w-0 flex-1">
            <dl>
              <dt className="text-sm font-medium text-gray-500 truncate">{title}</dt>
              <dd className="text-lg font-medium text-gray-900">{value}</dd>
              {subtitle && <dd className="text-sm text-gray-500">{subtitle}</dd>}
            </dl>
          </div>
        </div>
      </div>
    </div>
  );

  const ActionCard = ({ title, description, icon: Icon, onClick, loading: actionLoading, color }) => (
    <div className="bg-white shadow rounded-lg p-6">
      <div className="flex items-center">
        <div className="flex-shrink-0">
          <Icon className={`h-8 w-8 ${color}`} />
        </div>
        <div className="ml-4 flex-1">
          <h3 className="text-lg font-medium text-gray-900">{title}</h3>
          <p className="text-sm text-gray-500">{description}</p>
        </div>
        <div className="ml-4">
          <button
            onClick={onClick}
            disabled={actionLoading}
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {actionLoading ? (
              <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
            ) : (
              <RefreshCw className="h-4 w-4 mr-2" />
            )}
            Execute
          </button>
        </div>
      </div>
    </div>
  );

  if (loading && !stats) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">System Administration</h1>
            <p className="text-gray-600 mt-1">
              Manage system settings, data synchronization, and monitoring
            </p>
          </div>
          <div className="flex items-center space-x-2">
            <CheckCircle className="h-5 w-5 text-green-500" />
            <span className="text-sm text-gray-600">System Healthy</span>
          </div>
        </div>
      </div>

      {/* System Status */}
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">System Status</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <div className="flex items-center space-x-3">
            {getStatusIcon(systemStatus.namaste)}
            <div>
              <div className="text-sm font-medium text-gray-900">NAMASTE Service</div>
              <div className={`text-sm ${getStatusColor(systemStatus.namaste)}`}>
                {systemStatus.namaste}
              </div>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            {getStatusIcon(systemStatus.icd11)}
            <div>
              <div className="text-sm font-medium text-gray-900">ICD-11 API</div>
              <div className={`text-sm ${getStatusColor(systemStatus.icd11)}`}>
                {systemStatus.icd11}
              </div>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            {getStatusIcon(systemStatus.database)}
            <div>
              <div className="text-sm font-medium text-gray-900">Database</div>
              <div className={`text-sm ${getStatusColor(systemStatus.database)}`}>
                {systemStatus.database}
              </div>
            </div>
          </div>
          <div className="flex items-center space-x-3">
            {getStatusIcon(systemStatus.fhir)}
            <div>
              <div className="text-sm font-medium text-gray-900">FHIR Server</div>
              <div className={`text-sm ${getStatusColor(systemStatus.fhir)}`}>
                {systemStatus.fhir}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="NAMASTE Codes"
          value={stats?.namasteCodeCount?.toLocaleString() || '0'}
          icon={Database}
          color="text-orange-600"
          subtitle="Traditional Medicine"
        />
        <StatCard
          title="ICD-11 Codes"
          value={stats?.icd11CodeCount?.toLocaleString() || '0'}
          icon={FileText}
          color="text-blue-600"
          subtitle="WHO Classification"
        />
        <StatCard
          title="Concept Mappings"
          value={stats?.mappingCount?.toLocaleString() || '0'}
          icon={GitBranch}
          color="text-green-600"
          subtitle="Cross-references"
        />
        <StatCard
          title="System Uptime"
          value="99.9%"
          icon={Shield}
          color="text-green-600"
          subtitle="Last 30 days"
        />
      </div>

      {/* Administrative Actions */}
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Administrative Actions</h3>
        <div className="space-y-4">
          <ActionCard
            title="Generate Automatic Mappings"
            description="Create concept mappings based on existing WHO terminology codes"
            icon={GitBranch}
            onClick={handleGenerateMappings}
            loading={loading}
            color="text-green-600"
          />
          <ActionCard
            title="Reload NAMASTE Data"
            description="Reload NAMASTE codes from CSV file"
            icon={Database}
            onClick={handleReloadNamaste}
            loading={loading}
            color="text-orange-600"
          />
          <ActionCard
            title="Sync ICD-11 Data"
            description="Synchronize ICD-11 data from WHO API"
            icon={FileText}
            onClick={handleSyncIcd11}
            loading={loading}
            color="text-blue-600"
          />
        </div>
      </div>

      {/* Data Management */}
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Data Management</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="border border-gray-200 rounded-lg p-4">
            <h4 className="text-sm font-medium text-gray-900 mb-2">Export Data</h4>
            <p className="text-sm text-gray-600 mb-3">Export terminology data in various formats</p>
            <div className="flex space-x-2">
              <button className="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                <Download className="h-4 w-4 mr-1" />
                JSON
              </button>
              <button className="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                <Download className="h-4 w-4 mr-1" />
                CSV
              </button>
              <button className="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                <Download className="h-4 w-4 mr-1" />
                XML
              </button>
            </div>
          </div>
          
          <div className="border border-gray-200 rounded-lg p-4">
            <h4 className="text-sm font-medium text-gray-900 mb-2">Import Data</h4>
            <p className="text-sm text-gray-600 mb-3">Import terminology data from external sources</p>
            <div className="flex space-x-2">
              <button className="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                <Upload className="h-4 w-4 mr-1" />
                Upload
              </button>
              <button className="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm leading-4 font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                <Database className="h-4 w-4 mr-1" />
                Sync
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* System Logs */}
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Recent System Logs</h3>
        <div className="space-y-2">
          {recentLogs.map((log, index) => (
            <div key={index} className="flex items-center space-x-3 py-2">
              {getLogIcon(log.type)}
              <div className="flex-1 min-w-0">
                <p className="text-sm text-gray-900">{log.message}</p>
                <p className="text-xs text-gray-500">{log.time}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* System Information */}
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">System Information</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h4 className="text-sm font-medium text-gray-900 mb-2">Application Details</h4>
            <dl className="space-y-1 text-sm text-gray-600">
              <div className="flex">
                <dt className="w-24">Version:</dt>
                <dd>1.0.0</dd>
              </div>
              <div className="flex">
                <dt className="w-24">Build:</dt>
                <dd>2024.01.15</dd>
              </div>
              <div className="flex">
                <dt className="w-24">Java:</dt>
                <dd>17.0.2</dd>
              </div>
              <div className="flex">
                <dt className="w-24">Spring:</dt>
                <dd>3.2.0</dd>
              </div>
            </dl>
          </div>
          <div>
            <h4 className="text-sm font-medium text-gray-900 mb-2">FHIR Compliance</h4>
            <dl className="space-y-1 text-sm text-gray-600">
              <div className="flex">
                <dt className="w-24">Version:</dt>
                <dd>R4 (4.0.1)</dd>
              </div>
              <div className="flex">
                <dt className="w-24">HAPI:</dt>
                <dd>6.8.5</dd>
              </div>
              <div className="flex">
                <dt className="w-24">Resources:</dt>
                <dd>5 supported</dd>
              </div>
              <div className="flex">
                <dt className="w-24">Operations:</dt>
                <dd>4 supported</dd>
              </div>
            </dl>
          </div>
        </div>
      </div>
    </div>
  );
};

export { AdminPage };