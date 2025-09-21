import axios from 'axios';
import toast from 'react-hot-toast';
import { API_ENDPOINTS, TOAST_MESSAGES } from '../constants';

// Create axios instance
const api = axios.create({
  baseURL: 'http://localhost:8080/fhir-terminology/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Setup interceptors
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const apiError = {
      message: error.response?.data?.message || error.message || 'An error occurred',
      status: error.response?.status || 500,
      timestamp: new Date().toISOString(),
      path: error.config?.url || 'unknown'
    };

    if (apiError.status === 401) {
      localStorage.removeItem('auth_token');
      window.location.href = '/login';
    } else if (apiError.status >= 500) {
      toast.error(TOAST_MESSAGES.ERROR.NETWORK);
    } else if (apiError.status >= 400) {
      toast.error(apiError.message);
    }

    return Promise.reject(apiError);
  }
);

// API service object
const apiService = {
  // NAMASTE Code endpoints
  async searchNamasteCodes(term, page = 0, size = 20) {
    const response = await api.get(API_ENDPOINTS.NAMASTE_SEARCH, {
      params: { term, page, size }
    });
    return response.data;
  },

  async getNamasteCode(code) {
    const response = await api.get(`${API_ENDPOINTS.NAMASTE_CODE}/${code}`);
    return response.data;
  },

  async getNamasteAutoComplete(term, limit = 10) {
    const response = await api.get(API_ENDPOINTS.NAMASTE_AUTOCOMPLETE, {
      params: { term, limit }
    });
    return response.data;
  },

  async getNamasteBySystem(system) {
    const response = await api.get(`${API_ENDPOINTS.NAMASTE_SYSTEM}/${system}`);
    return response.data;
  },

  async getNamasteCategories(system) {
    const response = await api.get(`${API_ENDPOINTS.NAMASTE_CATEGORIES}/${system}`);
    return response.data;
  },

  // ICD-11 Code endpoints
  async searchIcd11Codes(term, page = 0, size = 20) {
    const response = await api.get(API_ENDPOINTS.ICD11_SEARCH, {
      params: { term, page, size }
    });
    return response.data;
  },

  async getIcd11Code(code) {
    const response = await api.get(`${API_ENDPOINTS.ICD11_CODE}/${code}`);
    return response.data;
  },

  async getIcd11AutoComplete(term, limit = 10) {
    const response = await api.get(API_ENDPOINTS.ICD11_AUTOCOMPLETE, {
      params: { term, limit }
    });
    return response.data;
  },

  async getIcd11ByType(type) {
    const response = await api.get(`${API_ENDPOINTS.ICD11_TYPE}/${type}`);
    return response.data;
  },

  // Translation endpoints
  async translateNamasteToTm2(code) {
    const response = await api.get(`${API_ENDPOINTS.TRANSLATE_NAMASTE_TO_TM2}/${code}`);
    return response.data;
  },

  async translateTm2ToNamaste(code) {
    const response = await api.get(`${API_ENDPOINTS.TRANSLATE_TM2_TO_NAMASTE}/${code}`);
    return response.data;
  },

  async translateNamasteToBiomedicine(code) {
    const response = await api.get(`${API_ENDPOINTS.TRANSLATE_NAMASTE_TO_BIOMEDICINE}/${code}`);
    return response.data;
  },

  // Mapping management endpoints
  async createMapping(request) {
    const response = await api.post(API_ENDPOINTS.MAPPING, request);
    return response.data;
  },

  async getMappingsForCode(system, code) {
    const response = await api.get(`${API_ENDPOINTS.MAPPING}/${system}/${code}`);
    return response.data;
  },

  async deleteMapping(id) {
    await api.delete(`${API_ENDPOINTS.MAPPING}/${id}`);
  },

  // Statistics endpoint
  async getStats() {
    const response = await api.get(API_ENDPOINTS.STATS);
    return response.data;
  },

  // Administrative endpoints
  async generateMappings() {
    const response = await api.post(API_ENDPOINTS.ADMIN_GENERATE_MAPPINGS);
    return response.data;
  },

  async reloadNamasteData() {
    const response = await api.post(API_ENDPOINTS.ADMIN_RELOAD_NAMASTE);
    return response.data;
  },

  async syncIcd11Data() {
    const response = await api.post(API_ENDPOINTS.ADMIN_SYNC_ICD11);
    return response.data;
  },

  // FHIR endpoints
  async getFhirMetadata(format = 'json') {
    const response = await api.get(API_ENDPOINTS.FHIR_METADATA, {
      params: { _format: format }
    });
    return response.data;
  },

  async getNamasteCodeSystem(format = 'json') {
    const response = await api.get(API_ENDPOINTS.FHIR_CODESYSTEM, {
      params: { _format: format }
    });
    return response.data;
  },

  async getNamasteToIcd11ConceptMap(format = 'json') {
    const response = await api.get(API_ENDPOINTS.FHIR_CONCEPTMAP, {
      params: { _format: format }
    });
    return response.data;
  },

  async getNamasteValueSet(filter, system, format = 'json') {
    const params = { _format: format };
    if (filter) params.filter = filter;
    if (system) params.system = system;

    const response = await api.get(API_ENDPOINTS.FHIR_VALUESET, { params });
    return response.data;
  },

  async translateConcept(code, system, targetSystem, format = 'json') {
    const params = { code, system, _format: format };
    if (targetSystem) params.targetsystem = targetSystem;

    const response = await api.post(API_ENDPOINTS.FHIR_TRANSLATE, null, { params });
    return response.data;
  },

  async uploadBundle(bundle, format = 'json') {
    const response = await api.post(API_ENDPOINTS.FHIR_BUNDLE, bundle, {
      params: { _format: format }
    });
    return response.data;
  },

  async createCondition(request, format = 'json') {
    const response = await api.post(API_ENDPOINTS.FHIR_CONDITION, request, {
      params: { _format: format }
    });
    return response.data;
  },

  // Problem List endpoints
  async createDualCodedCondition(request, format = 'json') {
    const response = await api.post('/fhir/ProblemList/Condition', request, {
      params: { _format: format }
    });
    return response.data;
  },

  async processDualCodedBundle(bundle, format = 'json') {
    const response = await api.post('/fhir/ProblemList/Bundle', bundle, {
      params: { _format: format }
    });
    return response.data;
  },

  async getAutoCompleteSuggestions(term, limit = 10, format = 'json') {
    const response = await api.get('/fhir/ProblemList/ValueSet/dual-coding-autocomplete', {
      params: { term, limit, _format: format }
    });
    return response.data;
  },

  // Utility methods
  setAuthToken(token) {
    localStorage.setItem('auth_token', token);
  },

  clearAuthToken() {
    localStorage.removeItem('auth_token');
  },

  getAuthToken() {
    return localStorage.getItem('auth_token');
  }
};

export default apiService;