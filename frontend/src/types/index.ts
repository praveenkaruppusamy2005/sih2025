// Core data models
export interface NamasteCode {
  id: number;
  code: string;
  display: string;
  definition?: string;
  system: TraditionalSystem;
  category?: string;
  subcategory?: string;
  whoTerminologyCode?: string;
  icd11Tm2Code?: string;
  icd11BiomedicineCode?: string;
  createdAt: string;
  updatedAt: string;
  version?: string;
}

export interface Icd11Code {
  id: number;
  code: string;
  title: string;
  definition?: string;
  codeType: CodeType;
  parent?: string;
  chapter?: string;
  synonyms?: Record<string, string>;
  linearizationUri?: string;
  foundationUri?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ConceptMapping {
  id: number;
  sourceCode: string;
  sourceSystem: string;
  targetCode: string;
  targetSystem: string;
  equivalence: MappingEquivalence;
  comment?: string;
  confidenceScore?: number;
  mappingVersion?: string;
  createdAt: string;
  updatedAt: string;
}

export interface SearchResult {
  code: string;
  display: string;
  system: string;
  definition?: string;
  mappings?: ConceptMapping[];
  score?: number;
}

// Enums
export enum TraditionalSystem {
  AYURVEDA = 'AYURVEDA',
  SIDDHA = 'SIDDHA',
  UNANI = 'UNANI'
}

export enum CodeType {
  TM2 = 'TM2',
  BIOMEDICINE = 'BIOMEDICINE'
}

export enum MappingEquivalence {
  RELATEDTO = 'RELATEDTO',
  EQUIVALENT = 'EQUIVALENT',
  EQUAL = 'EQUAL',
  WIDER = 'WIDER',
  SUBSUMES = 'SUBSUMES',
  NARROWER = 'NARROWER',
  SPECIALIZES = 'SPECIALIZES',
  INEXACT = 'INEXACT',
  UNMATCHED = 'UNMATCHED',
  DISJOINT = 'DISJOINT'
}

// API Response types
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

export interface TerminologyStats {
  namasteCodeCount: number;
  icd11CodeCount: number;
  mappingCount: number;
  ayurvedaCount: number;
  siddhaCount: number;
  unaniCount: number;
  tm2Count: number;
  biomedicineCount: number;
}

// Request/Response DTOs
export interface CreateMappingRequest {
  sourceCode: string;
  sourceSystem: string;
  targetCode: string;
  targetSystem: string;
  equivalence: MappingEquivalence;
  comment?: string;
  confidenceScore?: number;
}

export interface CreateConditionRequest {
  namasteCode: string;
  patientId: string;
}

// UI State types
export interface SearchFilters {
  system?: TraditionalSystem;
  category?: string;
  codeType?: CodeType;
  chapter?: string;
}

export interface SearchParams {
  term: string;
  page: number;
  size: number;
  filters?: SearchFilters;
}

// FHIR types
export interface FhirBundle {
  resourceType: string;
  type: string;
  entry: FhirEntry[];
}

export interface FhirEntry {
  resource: FhirResource;
}

export interface FhirResource {
  resourceType: string;
  id?: string;
  [key: string]: any;
}

export interface FhirCondition extends FhirResource {
  resourceType: 'Condition';
  code: {
    coding: FhirCoding[];
  };
  subject: {
    reference: string;
  };
}

export interface FhirCoding {
  system: string;
  code: string;
  display: string;
}

// Error types
export interface ApiError {
  message: string;
  status: number;
  timestamp: string;
  path: string;
}

// Authentication types
export interface User {
  id: string;
  name: string;
  email: string;
  roles: string[];
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
}

// Chart data types
export interface ChartData {
  name: string;
  value: number;
  color?: string;
}

export interface TimeSeriesData {
  date: string;
  value: number;
  label?: string;
}
