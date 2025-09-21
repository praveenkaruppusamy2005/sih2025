import axios, { AxiosInstance, AxiosResponse } from 'axios';
import toast from 'react-hot-toast';
import {
  NamasteCode,
  Icd11Code,
  ConceptMapping,
  SearchResult,
  TerminologyStats,
  CreateMappingRequest,
  CreateConditionRequest,
  PaginatedResponse,
  TraditionalSystem,
  CodeType,
  FhirBundle,
  ApiError
} from '../types';

class ApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: '/fhir-terminology/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.api.interceptors.request.use(
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

    // Response interceptor
    this.api.interceptors.response.use(
      (response: AxiosResponse) => {
        return response;
      },
      (error) => {
        const apiError: ApiError = {
          message: error.response?.data?.message || error.message || 'An error occurred',
          status: error.response?.status || 500,
          timestamp: new Date().toISOString(),
          path: error.config?.url || 'unknown'
        };

        if (apiError.status === 401) {
          localStorage.removeItem('auth_token');
          window.location.href = '/login';
        } else if (apiError.status >= 500) {
          toast.error('Server error. Please try again later.');
        } else if (apiError.status >= 400) {
          toast.error(apiError.message);
        }

        return Promise.reject(apiError);
      }
    );
  }

  // NAMASTE Code endpoints
  async searchNamasteCodes(term: string, page: number = 0, size: number = 20): Promise<PaginatedResponse<NamasteCode>> {
    const response = await this.api.get('/terminology/namaste/search', {
      params: { term, page, size }
    });
    return response.data;
  }

  async getNamasteCode(code: string): Promise<NamasteCode> {
    const response = await this.api.get(`/terminology/namaste/code/${code}`);
    return response.data;
  }

  async getNamasteAutoComplete(term: string, limit: number = 10): Promise<NamasteCode[]> {
    const response = await this.api.get('/terminology/namaste/autocomplete', {
      params: { term, limit }
    });
    return response.data;
  }

  async getNamasteBySystem(system: TraditionalSystem): Promise<NamasteCode[]> {
    const response = await this.api.get(`/terminology/namaste/system/${system}`);
    return response.data;
  }

  async getNamasteCategories(system: TraditionalSystem): Promise<string[]> {
    const response = await this.api.get(`/terminology/namaste/categories/${system}`);
    return response.data;
  }

  // ICD-11 Code endpoints
  async searchIcd11Codes(term: string, page: number = 0, size: number = 20): Promise<PaginatedResponse<Icd11Code>> {
    const response = await this.api.get('/terminology/icd11/search', {
      params: { term, page, size }
    });
    return response.data;
  }

  async getIcd11Code(code: string): Promise<Icd11Code> {
    const response = await this.api.get(`/terminology/icd11/code/${code}`);
    return response.data;
  }

  async getIcd11AutoComplete(term: string, limit: number = 10): Promise<Icd11Code[]> {
    const response = await this.api.get('/terminology/icd11/autocomplete', {
      params: { term, limit }
    });
    return response.data;
  }

  async getIcd11ByType(type: CodeType): Promise<Icd11Code[]> {
    const response = await this.api.get(`/terminology/icd11/type/${type}`);
    return response.data;
  }

  // Translation endpoints
  async translateNamasteToTm2(code: string): Promise<ConceptMapping[]> {
    const response = await this.api.get(`/terminology/translate/namaste-to-tm2/${code}`);
    return response.data;
  }

  async translateTm2ToNamaste(code: string): Promise<ConceptMapping[]> {
    const response = await this.api.get(`/terminology/translate/tm2-to-namaste/${code}`);
    return response.data;
  }

  async translateNamasteToBiomedicine(code: string): Promise<ConceptMapping[]> {
    const response = await this.api.get(`/terminology/translate/namaste-to-biomedicine/${code}`);
    return response.data;
  }

  // Mapping management endpoints
  async createMapping(request: CreateMappingRequest): Promise<ConceptMapping> {
    const response = await this.api.post('/terminology/mapping', request);
    return response.data;
  }

  async getMappingsForCode(system: string, code: string): Promise<ConceptMapping[]> {
    const response = await this.api.get(`/terminology/mapping/${system}/${code}`);
    return response.data;
  }

  async deleteMapping(id: number): Promise<void> {
    await this.api.delete(`/terminology/mapping/${id}`);
  }

  // Statistics endpoint
  async getStats(): Promise<TerminologyStats> {
    const response = await this.api.get('/terminology/stats');
    return response.data;
  }

  // Administrative endpoints
  async generateMappings(): Promise<string> {
    const response = await this.api.post('/terminology/admin/generate-mappings');
    return response.data;
  }

  async reloadNamasteData(): Promise<string> {
    const response = await this.api.post('/terminology/admin/reload-namaste');
    return response.data;
  }

  async syncIcd11Data(): Promise<string> {
    const response = await this.api.post('/terminology/admin/sync-icd11');
    return response.data;
  }

  // FHIR endpoints
  async getFhirMetadata(format: string = 'json'): Promise<string> {
    const response = await this.api.get('/fhir/metadata', {
      params: { _format: format }
    });
    return response.data;
  }

  async getNamasteCodeSystem(format: string = 'json'): Promise<string> {
    const response = await this.api.get('/fhir/CodeSystem/namaste-codes', {
      params: { _format: format }
    });
    return response.data;
  }

  async getNamasteToIcd11ConceptMap(format: string = 'json'): Promise<string> {
    const response = await this.api.get('/fhir/ConceptMap/namaste-to-icd11', {
      params: { _format: format }
    });
    return response.data;
  }

  async getNamasteValueSet(filter?: string, system?: TraditionalSystem, format: string = 'json'): Promise<string> {
    const params: any = { _format: format };
    if (filter) params.filter = filter;
    if (system) params.system = system;

    const response = await this.api.get('/fhir/ValueSet/namaste', { params });
    return response.data;
  }

  async translateConcept(code: string, system: string, targetSystem?: string, format: string = 'json'): Promise<string> {
    const params: any = { code, system, _format: format };
    if (targetSystem) params.targetsystem = targetSystem;

    const response = await this.api.post('/fhir/ConceptMap/namaste-to-icd11/$translate', null, { params });
    return response.data;
  }

  async uploadBundle(bundle: FhirBundle, format: string = 'json'): Promise<string> {
    const response = await this.api.post('/fhir/Bundle', bundle, {
      params: { _format: format }
    });
    return response.data;
  }

  async createCondition(request: CreateConditionRequest, format: string = 'json'): Promise<string> {
    const response = await this.api.post('/fhir/Condition', request, {
      params: { _format: format }
    });
    return response.data;
  }

  // Utility methods
  setAuthToken(token: string) {
    localStorage.setItem('auth_token', token);
  }

  clearAuthToken() {
    localStorage.removeItem('auth_token');
  }

  getAuthToken(): string | null {
    return localStorage.getItem('auth_token');
  }
}

export const apiService = new ApiService();
export default apiService;
