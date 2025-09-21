# NAMASTE Terminology Frontend

A modern React frontend for the NAMASTE-ICD11 FHIR Terminology Service, providing a comprehensive interface for traditional medicine code management and FHIR-compliant operations.

## Features

### 🏠 Dashboard
- System overview with statistics
- Quick access to all features
- Real-time system status
- Interactive charts and visualizations

### 🔍 Search Functionality
- Advanced search for NAMASTE and ICD-11 codes
- Filtering by traditional medicine systems (Ayurveda, Siddha, Unani)
- Filtering by ICD-11 code types (TM2, Biomedicine)
- Pagination and auto-complete support
- Export functionality

### 🔗 Concept Mapping
- Create and manage concept mappings between code systems
- Visual mapping relationships
- Equivalence types (Equivalent, Related, Inexact, etc.)
- Confidence scoring
- Bulk operations

### 📋 FHIR Operations
- FHIR R4 compliant terminology server
- Metadata, CodeSystem, and ConceptMap endpoints
- Code translation operations
- Bundle upload and processing
- Condition creation with dual coding
- JSON/XML format support

### ⚙️ Administration
- System health monitoring
- Data synchronization tools
- Automatic mapping generation
- Activity logs and audit trails
- System statistics and analytics

## Technology Stack

- **React 18** - Modern React with hooks
- **React Router** - Client-side routing
- **React Query** - Data fetching and caching
- **React Hook Form** - Form management
- **Tailwind CSS** - Utility-first CSS framework
- **Lucide React** - Beautiful icons
- **Recharts** - Data visualization
- **Axios** - HTTP client
- **React Hot Toast** - Notifications
- **React Syntax Highlighter** - Code display

## Getting Started

### Prerequisites
- Node.js 16+ 
- npm or yarn
- Backend API running on port 8080

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm start
```

3. Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

### Building for Production

```bash
npm run build
```

This builds the app for production to the `build` folder.

## Project Structure

```
src/
├── components/          # Reusable UI components
│   └── Layout.jsx      # Main layout component
├── pages/              # Page components
│   ├── Dashboard.jsx   # Dashboard page
│   ├── SearchPage.jsx  # Search functionality
│   ├── MappingPage.jsx # Concept mapping
│   ├── FhirPage.jsx    # FHIR operations
│   ├── AdminPage.jsx   # Administration
│   └── NotFound.jsx    # 404 page
├── services/           # API services
│   └── api.js         # Main API service
├── constants/          # Constants and configuration
│   └── index.js       # Application constants
├── App.jsx            # Main app component
├── index.jsx          # Application entry point
└── index.css          # Global styles
```

## API Integration

The frontend integrates with the Spring Boot backend through RESTful APIs:

- **Base URL**: `/fhir-terminology/api`
- **Authentication**: JWT Bearer tokens
- **Content Type**: `application/json`
- **Error Handling**: Centralized error handling with toast notifications

### Key Endpoints

- `GET /terminology/namaste/search` - Search NAMASTE codes
- `GET /terminology/icd11/search` - Search ICD-11 codes
- `GET /terminology/mapping/{system}/{code}` - Get mappings
- `POST /terminology/mapping` - Create mapping
- `GET /fhir/metadata` - FHIR metadata
- `POST /fhir/ConceptMap/namaste-to-icd11/$translate` - Translate codes

## Styling

The application uses Tailwind CSS with custom components:

- **Color Scheme**: Traditional medicine system colors
- **Components**: Reusable button, input, card, and badge styles
- **Responsive**: Mobile-first responsive design
- **Accessibility**: WCAG compliant components

## State Management

- **React Query**: Server state management
- **React Hook Form**: Form state management
- **Local State**: Component-level state with useState
- **Context**: Authentication and theme context (if needed)

## Error Handling

- **API Errors**: Centralized error handling in API service
- **Form Validation**: Client-side validation with error messages
- **User Feedback**: Toast notifications for all operations
- **Fallback UI**: Error boundaries and fallback components

## Performance

- **Code Splitting**: Route-based code splitting
- **Lazy Loading**: Component lazy loading
- **Caching**: React Query caching for API responses
- **Optimization**: Memoization and optimization techniques

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the Government of India License.

## Support

For support and questions, contact:
- Email: support@ayush.gov.in
- Website: https://ayush.gov.in
