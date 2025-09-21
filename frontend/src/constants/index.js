// Traditional Medicine Systems
export const TRADITIONAL_SYSTEMS = {
  AYURVEDA: 'AYURVEDA',
  SIDDHA: 'SIDDHA',
  UNANI: 'UNANI'
};

// ICD-11 Code Types
export const CODE_TYPES = {
  TM2: 'TM2',
  BIOMEDICINE: 'BIOMEDICINE'
};

// Mapping Equivalence Types
export const MAPPING_EQUIVALENCE = {
  RELATEDTO: 'RELATEDTO',
  EQUIVALENT: 'EQUIVALENT',
  EQUAL: 'EQUAL',
  WIDER: 'WIDER',
  SUBSUMES: 'SUBSUMES',
  NARROWER: 'NARROWER',
  SPECIALIZES: 'SPECIALIZES',
  INEXACT: 'INEXACT',
  UNMATCHED: 'UNMATCHED',
  DISJOINT: 'DISJOINT'
};

// API Endpoints
export const API_ENDPOINTS = {
  NAMASTE_SEARCH: '/terminology/namaste/search',
  NAMASTE_CODE: '/terminology/namaste/code',
  NAMASTE_AUTOCOMPLETE: '/terminology/namaste/autocomplete',
  NAMASTE_SYSTEM: '/terminology/namaste/system',
  NAMASTE_CATEGORIES: '/terminology/namaste/categories',
  ICD11_SEARCH: '/terminology/icd11/search',
  ICD11_CODE: '/terminology/icd11/code',
  ICD11_AUTOCOMPLETE: '/terminology/icd11/autocomplete',
  ICD11_TYPE: '/terminology/icd11/type',
  TRANSLATE_NAMASTE_TO_TM2: '/terminology/translate/namaste-to-tm2',
  TRANSLATE_TM2_TO_NAMASTE: '/terminology/translate/tm2-to-namaste',
  TRANSLATE_NAMASTE_TO_BIOMEDICINE: '/terminology/translate/namaste-to-biomedicine',
  MAPPING: '/terminology/mapping',
  STATS: '/terminology/stats',
  ADMIN_GENERATE_MAPPINGS: '/terminology/admin/generate-mappings',
  ADMIN_RELOAD_NAMASTE: '/terminology/admin/reload-namaste',
  ADMIN_SYNC_ICD11: '/terminology/admin/sync-icd11',
  FHIR_METADATA: '/fhir/metadata',
  FHIR_CODESYSTEM: '/fhir/CodeSystem/namaste-codes',
  FHIR_CONCEPTMAP: '/fhir/ConceptMap/namaste-to-icd11',
  FHIR_VALUESET: '/fhir/ValueSet/namaste',
  FHIR_TRANSLATE: '/fhir/ConceptMap/namaste-to-icd11/$translate',
  FHIR_BUNDLE: '/fhir/Bundle',
  FHIR_CONDITION: '/fhir/Condition'
};

// UI Constants
export const PAGINATION_DEFAULTS = {
  PAGE_SIZE: 20,
  MAX_PAGE_SIZE: 100,
  AUTOCOMPLETE_LIMIT: 10,
  MAX_AUTOCOMPLETE_LIMIT: 50
};

// Color Schemes for Different Systems
export const SYSTEM_COLORS = {
  AYURVEDA: {
    primary: '#f17a0a',
    light: '#fef7ee',
    dark: '#782f0f'
  },
  SIDDHA: {
    primary: '#22c55e',
    light: '#f0fdf4',
    dark: '#14532d'
  },
  UNANI: {
    primary: '#ef4444',
    light: '#fef2f2',
    dark: '#7f1d1d'
  },
  TM2: {
    primary: '#3b82f6',
    light: '#eff6ff',
    dark: '#1e3a8a'
  },
  BIOMEDICINE: {
    primary: '#8b5cf6',
    light: '#f3e8ff',
    dark: '#581c87'
  }
};

// Toast Messages
export const TOAST_MESSAGES = {
  SUCCESS: {
    MAPPING_CREATED: 'Concept mapping created successfully',
    DATA_RELOADED: 'Data reloaded successfully',
    SYNC_COMPLETED: 'Synchronization completed',
    CONDITION_CREATED: 'FHIR Condition created successfully'
  },
  ERROR: {
    GENERIC: 'An error occurred. Please try again.',
    NETWORK: 'Network error. Please check your connection.',
    UNAUTHORIZED: 'You are not authorized to perform this action.',
    NOT_FOUND: 'The requested resource was not found.',
    VALIDATION: 'Please check your input and try again.'
  },
  INFO: {
    LOADING: 'Loading...',
    PROCESSING: 'Processing your request...',
    SYNC_IN_PROGRESS: 'Synchronization in progress...'
  }
};

// Form Validation Rules
export const VALIDATION_RULES = {
  CODE: {
    MIN_LENGTH: 3,
    MAX_LENGTH: 50,
    PATTERN: /^[A-Z0-9_-]+$/i
  },
  DISPLAY: {
    MIN_LENGTH: 3,
    MAX_LENGTH: 200
  },
  DEFINITION: {
    MAX_LENGTH: 1000
  },
  COMMENT: {
    MAX_LENGTH: 500
  }
};

// FHIR Resource Types
export const FHIR_RESOURCE_TYPES = {
  CONDITION: 'Condition',
  BUNDLE: 'Bundle',
  CODESYSTEM: 'CodeSystem',
  CONCEPTMAP: 'ConceptMap',
  VALUESET: 'ValueSet',
  PARAMETERS: 'Parameters',
  OPERATIONOUTCOME: 'OperationOutcome'
};

// Chart Configuration
export const CHART_CONFIG = {
  COLORS: [
    '#3b82f6', '#ef4444', '#22c55e', '#f59e0b',
    '#8b5cf6', '#06b6d4', '#84cc16', '#f97316',
    '#ec4899', '#6366f1', '#14b8a6', '#eab308'
  ],
  DEFAULT_HEIGHT: 300,
  RESPONSIVE_HEIGHT: 250
};
