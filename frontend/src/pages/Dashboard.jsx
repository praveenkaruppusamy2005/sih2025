import React, { useState, useEffect } from 'react';
import { 
  Search, 
  GitBranch, 
  FileText, 
  Activity, 
  TrendingUp,
  Users,
  Database,
  Shield,
  AlertCircle,
  CheckCircle,
  Clock
} from 'lucide-react';
import { Link } from 'react-router-dom';
import apiService from '../services/api';
import { SYSTEM_COLORS } from '../constants';

const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [recentActivity, setRecentActivity] = useState([]);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [statsData] = await Promise.all([
        apiService.getStats()
      ]);
      setStats(statsData);
      setRecentActivity([
        { type: 'search', message: 'NAMASTE code search performed', time: '2 minutes ago' },
        { type: 'mapping', message: 'New concept mapping created', time: '5 minutes ago' },
        { type: 'fhir', message: 'FHIR Condition created', time: '10 minutes ago' },
        { type: 'sync', message: 'ICD-11 data synchronized', time: '1 hour ago' }
      ]);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
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

  const QuickActionCard = ({ title, description, icon: Icon, href, color }) => (
    <Link
      to={href}
      className="block bg-white overflow-hidden shadow rounded-lg hover:shadow-md transition-shadow duration-200"
    >
      <div className="p-6">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <Icon className={`h-8 w-8 ${color}`} />
          </div>
          <div className="ml-4">
            <h3 className="text-lg font-medium text-gray-900">{title}</h3>
            <p className="text-sm text-gray-500">{description}</p>
          </div>
        </div>
      </div>
    </Link>
  );

  const ActivityItem = ({ activity }) => {
    const getIcon = (type) => {
      switch (type) {
        case 'search': return <Search className="h-4 w-4 text-blue-500" />;
        case 'mapping': return <GitBranch className="h-4 w-4 text-green-500" />;
        case 'fhir': return <FileText className="h-4 w-4 text-purple-500" />;
        case 'sync': return <Activity className="h-4 w-4 text-orange-500" />;
        default: return <Clock className="h-4 w-4 text-gray-500" />;
      }
    };

    return (
      <div className="flex items-center space-x-3 py-2">
        {getIcon(activity.type)}
        <div className="flex-1 min-w-0">
          <p className="text-sm text-gray-900">{activity.message}</p>
          <p className="text-xs text-gray-500">{activity.time}</p>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-md p-4">
        <div className="flex">
          <AlertCircle className="h-5 w-5 text-red-400" />
          <div className="ml-3">
            <h3 className="text-sm font-medium text-red-800">Error loading dashboard</h3>
            <p className="text-sm text-red-700 mt-1">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">NAMASTE-ICD11 Terminology Service</h1>
            <p className="text-gray-600 mt-1">
              FHIR R4 compliant terminology service for traditional medicine integration
            </p>
          </div>
          <div className="flex items-center space-x-2">
            <CheckCircle className="h-5 w-5 text-green-500" />
            <span className="text-sm text-gray-600">System Online</span>
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
          title="System Health"
          value="100%"
          icon={Shield}
          color="text-green-600"
          subtitle="Uptime"
        />
      </div>

      {/* System Breakdown */}
      <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">NAMASTE Systems</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="w-3 h-3 rounded-full mr-3" style={{ backgroundColor: SYSTEM_COLORS.AYURVEDA.primary }}></div>
                <span className="text-sm text-gray-700">Ayurveda</span>
              </div>
              <span className="text-sm font-medium text-gray-900">{stats?.ayurvedaCount || 0}</span>
            </div>
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="w-3 h-3 rounded-full mr-3" style={{ backgroundColor: SYSTEM_COLORS.SIDDHA.primary }}></div>
                <span className="text-sm text-gray-700">Siddha</span>
              </div>
              <span className="text-sm font-medium text-gray-900">{stats?.siddhaCount || 0}</span>
            </div>
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="w-3 h-3 rounded-full mr-3" style={{ backgroundColor: SYSTEM_COLORS.UNANI.primary }}></div>
                <span className="text-sm text-gray-700">Unani</span>
              </div>
              <span className="text-sm font-medium text-gray-900">{stats?.unaniCount || 0}</span>
            </div>
          </div>
        </div>

        <div className="bg-white shadow rounded-lg p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">ICD-11 Types</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="w-3 h-3 rounded-full mr-3" style={{ backgroundColor: SYSTEM_COLORS.TM2.primary }}></div>
                <span className="text-sm text-gray-700">TM2 (Traditional Medicine)</span>
              </div>
              <span className="text-sm font-medium text-gray-900">{stats?.tm2Count || 0}</span>
            </div>
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <div className="w-3 h-3 rounded-full mr-3" style={{ backgroundColor: SYSTEM_COLORS.BIOMEDICINE.primary }}></div>
                <span className="text-sm text-gray-700">Biomedicine</span>
              </div>
              <span className="text-sm font-medium text-gray-900">{stats?.biomedicineCount || 0}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h3>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <QuickActionCard
            title="Search Codes"
            description="Search NAMASTE and ICD-11 codes"
            icon={Search}
            href="/search"
            color="text-blue-600"
          />
          <QuickActionCard
            title="Manage Mappings"
            description="Create and manage concept mappings"
            icon={GitBranch}
            href="/mapping"
            color="text-green-600"
          />
          <QuickActionCard
            title="FHIR Resources"
            description="View FHIR CodeSystems and ConceptMaps"
            icon={FileText}
            href="/fhir"
            color="text-purple-600"
          />
          <QuickActionCard
            title="Problem List"
            description="Create dual-coded conditions"
            icon={Users}
            href="/problem-list"
            color="text-orange-600"
          />
          <QuickActionCard
            title="System Admin"
            description="Manage system settings and data"
            icon={TrendingUp}
            href="/admin"
            color="text-gray-600"
          />
        </div>
      </div>

      {/* Recent Activity */}
      <div className="bg-white shadow rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Recent Activity</h3>
        <div className="space-y-1">
          {recentActivity.map((activity, index) => (
            <ActivityItem key={index} activity={activity} />
          ))}
        </div>
      </div>
    </div>
  );
};

export { Dashboard };