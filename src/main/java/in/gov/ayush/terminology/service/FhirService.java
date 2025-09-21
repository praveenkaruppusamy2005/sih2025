package in.gov.ayush.terminology.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

import in.gov.ayush.terminology.model.ConceptMapping;
import in.gov.ayush.terminology.model.Icd11Code;
import in.gov.ayush.terminology.model.NamasteCode;
import in.gov.ayush.terminology.utils.AuditLogger;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.model.Enumerations;
import java.util.Date;
import java.util.List;

@Service
public class FhirService {

    private static final Logger logger = LoggerFactory.getLogger(FhirService.class);

    @Autowired
    private FhirContext fhirContext;

    @Autowired
    private IParser fhirJsonParser;

    @Autowired
    private NamasteService namasteService;

    @Autowired
    private Icd11Service icd11Service;

    @Autowired
    private ConceptMappingService mappingService;

    @Autowired
    private AuditLogger auditLogger;

    @Value("${fhir.server.base-url}")
    private String baseUrl;

    @Value("${fhir.namaste.code-system}")
    private String namasteSystem;

    @Value("${fhir.namaste.version}")
    private String namasteVersion;

    @Value("${fhir.icd11.code-system}")
    private String icd11System;

    @Value("${fhir.icd11.tm2-system}")
    private String tm2System;

    public CodeSystem generateNamasteCodeSystem() {
        logger.info("Generating NAMASTE CodeSystem...");

        CodeSystem codeSystem = new CodeSystem();
        codeSystem.setId("namaste-codes");
        codeSystem.setUrl(namasteSystem);
        codeSystem.setVersion(namasteVersion);
        codeSystem.setName("NAMASTE");
        codeSystem.setTitle("National AYUSH Morbidity & Standardized Terminologies Electronic");
        codeSystem.setStatus(Enumerations.PublicationStatus.ACTIVE);
        codeSystem.setDate(new Date());
        codeSystem.setPublisher("Ministry of AYUSH, Government of India");
        codeSystem.setDescription("Standardized terminology codes for Ayurveda, Siddha, and Unani disorders");
        codeSystem.setContent(CodeSystem.CodeSystemContentMode.COMPLETE);

        // Add properties
        codeSystem.addProperty()
                .setCode("system")
                .setDescription("Traditional medicine system")
                .setType(CodeSystem.PropertyType.STRING);

        codeSystem.addProperty()
                .setCode("category")
                .setDescription("Disorder category")
                .setType(CodeSystem.PropertyType.STRING);

        codeSystem.addProperty()
                .setCode("who-terminology")
                .setDescription("WHO Standardised International Terminology code")
                .setType(CodeSystem.PropertyType.STRING);

        // Add concepts
        List<NamasteCode> allCodes = namasteService.findBySystem(null); // Get all codes
        for (NamasteCode namasteCode : allCodes) {
            CodeSystem.ConceptDefinitionComponent concept = codeSystem.addConcept();
            concept.setCode(namasteCode.getCode());
            concept.setDisplay(namasteCode.getDisplay());

            if (namasteCode.getDefinition() != null) {
                concept.setDefinition(namasteCode.getDefinition());
            }

            // Add properties
            if (namasteCode.getSystem() != null) {
                concept.addProperty()
                        .setCode("system")
                        .setValue(new StringType(namasteCode.getSystem().name()));
            }

            if (namasteCode.getCategory() != null) {
                concept.addProperty()
                        .setCode("category")
                        .setValue(new StringType(namasteCode.getCategory()));
            }

            if (namasteCode.getWhoTerminologyCode() != null) {
                concept.addProperty()
                        .setCode("who-terminology")
                        .setValue(new StringType(namasteCode.getWhoTerminologyCode()));
            }
        }

        auditLogger.logFhirResourceGeneration("CodeSystem", "NAMASTE", allCodes.size());
        logger.info("Generated NAMASTE CodeSystem with {} concepts", allCodes.size());

        return codeSystem;
    }

    public ConceptMap generateNamasteToIcd11ConceptMap() {
        logger.info("Generating NAMASTE to ICD-11 ConceptMap...");

        ConceptMap conceptMap = new ConceptMap();
        conceptMap.setId("namaste-to-icd11");
        conceptMap.setUrl(baseUrl + "/ConceptMap/namaste-to-icd11");
        conceptMap.setVersion("1.0");
        conceptMap.setName("NAMASTEToICD11");
        conceptMap.setTitle("NAMASTE to ICD-11 Concept Mapping");
        conceptMap.setStatus(Enumerations.PublicationStatus.ACTIVE);
        conceptMap.setDate(new Date());
        conceptMap.setPublisher("Ministry of AYUSH, Government of India");
        conceptMap.setDescription("Mapping between NAMASTE codes and ICD-11 TM2/Biomedicine codes");

        // Set source and target
        conceptMap.setSource(new UriType(namasteSystem));
        conceptMap.setTarget(new UriType(icd11System));

        // Create group for NAMASTE to TM2 mappings
        ConceptMap.ConceptMapGroupComponent tm2Group = conceptMap.addGroup();
        tm2Group.setSource(namasteSystem);
        tm2Group.setTarget(tm2System);

        // Create group for NAMASTE to Biomedicine mappings
        ConceptMap.ConceptMapGroupComponent biomedicineGroup = conceptMap.addGroup();
        biomedicineGroup.setSource(namasteSystem);
        biomedicineGroup.setTarget(icd11System);

        // Add mappings
        List<ConceptMapping> allMappings = mappingService.getAllMappings();
        int tm2MappingCount = 0;
        int biomedicineMappingCount = 0;

        for (ConceptMapping mapping : allMappings) {
            if (namasteSystem.equals(mapping.getSourceSystem())) {
                ConceptMap.SourceElementComponent element = null;
                ConceptMap.ConceptMapGroupComponent targetGroup = null;

                if (tm2System.equals(mapping.getTargetSystem())) {
                    element = getOrCreateSourceElement(tm2Group, mapping.getSourceCode());
                    targetGroup = tm2Group;
                    tm2MappingCount++;
                } else if (icd11System.equals(mapping.getTargetSystem())) {
                    element = getOrCreateSourceElement(biomedicineGroup, mapping.getSourceCode());
                    targetGroup = biomedicineGroup;
                    biomedicineMappingCount++;
                }

                if (element != null) {
                    ConceptMap.TargetElementComponent target = element.addTarget();
                    target.setCode(mapping.getTargetCode());
                    target.setEquivalence(mapToFhirEquivalence(mapping.getEquivalence()));

                    if (mapping.getComment() != null) {
                        target.setComment(mapping.getComment());
                    }
                }
            }
        }

        auditLogger.logFhirResourceGeneration("ConceptMap", "NAMASTE_TO_ICD11",
                tm2MappingCount + biomedicineMappingCount);
        logger.info("Generated ConceptMap with {} TM2 mappings and {} Biomedicine mappings",
                tm2MappingCount, biomedicineMappingCount);

        return conceptMap;
    }

    private ConceptMap.SourceElementComponent getOrCreateSourceElement(
            ConceptMap.ConceptMapGroupComponent group, String sourceCode) {

        for (ConceptMap.SourceElementComponent element : group.getElement()) {
            if (sourceCode.equals(element.getCode())) {
                return element;
            }
        }

        ConceptMap.SourceElementComponent newElement = group.addElement();
        newElement.setCode(sourceCode);
        return newElement;
    }

    private Enumerations.ConceptMapEquivalence mapToFhirEquivalence(
            ConceptMapping.MappingEquivalence equivalence) {

        return switch (equivalence) {
            case EQUIVALENT -> Enumerations.ConceptMapEquivalence.EQUIVALENT;
            case EQUAL -> Enumerations.ConceptMapEquivalence.EQUAL;
            case WIDER -> Enumerations.ConceptMapEquivalence.WIDER;
            case SUBSUMES -> Enumerations.ConceptMapEquivalence.SUBSUMES;
            case NARROWER -> Enumerations.ConceptMapEquivalence.NARROWER;
            case SPECIALIZES -> Enumerations.ConceptMapEquivalence.SPECIALIZES;
            case INEXACT -> Enumerations.ConceptMapEquivalence.INEXACT;
            case UNMATCHED -> Enumerations.ConceptMapEquivalence.UNMATCHED;
            case DISJOINT -> Enumerations.ConceptMapEquivalence.DISJOINT;
            default -> Enumerations.ConceptMapEquivalence.RELATEDTO;
        };
    }


    public ValueSet generateNamasteValueSet(String filter, NamasteCode.TraditionalSystem system) {
        ValueSet valueSet = new ValueSet();
        valueSet.setId("namaste-valueset");
        valueSet.setUrl(baseUrl + "/ValueSet/namaste");
        valueSet.setVersion("1.0");
        valueSet.setName("NAMASTEValueSet");
        valueSet.setTitle("NAMASTE Value Set");

        // ✅ use the shared enum from Enumerations
        valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);

        valueSet.setDate(new Date());

        // Compose and include section
        ValueSet.ValueSetComposeComponent compose = valueSet.getCompose();
        ValueSet.ConceptSetComponent include = compose.addInclude();
        include.setSystem(namasteSystem);  // make sure this variable is defined

        // Add display filter if specified
        if (filter != null && !filter.trim().isEmpty()) {
            ValueSet.ConceptSetFilterComponent filterComponent = include.addFilter();
            filterComponent.setProperty("display");
            filterComponent.setOp(ValueSet.FilterOperator.REGEX);
            filterComponent.setValue(".*" + filter + ".*");
        }

        // Add system filter if specified
        if (system != null) {
            ValueSet.ConceptSetFilterComponent systemFilter = include.addFilter();
            systemFilter.setProperty("system");
            systemFilter.setOp(ValueSet.FilterOperator.EQUAL);
            systemFilter.setValue(system.name());
        }

        return valueSet;
    }

    public Bundle createFhirBundle(List<Condition> conditions, String patientId) {
        Bundle bundle = new Bundle();
        bundle.setId("namaste-encounter-" + System.currentTimeMillis());
        bundle.setType(Bundle.BundleType.COLLECTION);
        bundle.setTimestamp(new Date());

        // Add patient reference
        bundle.addEntry()
                .setResource(createPatientReference(patientId))
                .getRequest()
                .setMethod(Bundle.HTTPVerb.PUT)
                .setUrl("Patient/" + patientId);

        // Add conditions
        for (Condition condition : conditions) {
            bundle.addEntry()
                    .setResource(condition)
                    .getRequest()
                    .setMethod(Bundle.HTTPVerb.POST)
                    .setUrl("Condition");
        }

        auditLogger.logFhirBundleCreation(patientId, conditions.size());
        return bundle;
    }

    public Condition createConditionWithDualCoding(String namasteCode, String patientId) {
        Condition condition = new Condition();
        condition.setId("condition-" + System.currentTimeMillis());
        condition.setSubject(new Reference("Patient/" + patientId));
        condition.setClinicalStatus(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
                        .setCode("active")));
        condition.setVerificationStatus(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")
                        .setCode("confirmed")));

        // Create code with dual coding
        CodeableConcept code = new CodeableConcept();

        // Add NAMASTE coding
        NamasteCode namaste = namasteService.findByCode(namasteCode).orElse(null);
        if (namaste != null) {
            Coding namasteCoding = new Coding()
                    .setSystem(namasteSystem)
                    .setCode(namaste.getCode())
                    .setDisplay(namaste.getDisplay());
            code.addCoding(namasteCoding);

            // Add mapped ICD-11 TM2 coding if available
            List<ConceptMapping> tm2Mappings = mappingService.translateNamesteToTm2(namasteCode);
            for (ConceptMapping mapping : tm2Mappings) {
                Icd11Code icd11Code = icd11Service.findByCode(mapping.getTargetCode()).orElse(null);
                if (icd11Code != null) {
                    Coding tm2Coding = new Coding()
                            .setSystem(tm2System)
                            .setCode(icd11Code.getCode())
                            .setDisplay(icd11Code.getTitle());
                    code.addCoding(tm2Coding);
                }
            }

            // Add mapped ICD-11 Biomedicine coding if available
            List<ConceptMapping> biomedicineMappings =
                    mappingService.translateNamasteToBiomedicine(namasteCode);
            for (ConceptMapping mapping : biomedicineMappings) {
                Icd11Code icd11Code = icd11Service.findByCode(mapping.getTargetCode()).orElse(null);
                if (icd11Code != null) {
                    Coding biomedicineCoding = new Coding()
                            .setSystem(icd11System)
                            .setCode(icd11Code.getCode())
                            .setDisplay(icd11Code.getTitle());
                    code.addCoding(biomedicineCoding);
                }
            }
        }

        condition.setCode(code);
        condition.setRecordedDate(new Date());

        return condition;
    }

    private Patient createPatientReference(String patientId) {
        Patient patient = new Patient();
        patient.setId(patientId);
        // Add minimal patient data - in real implementation, this would come from patient service
        patient.addIdentifier()
                .setSystem("https://healthid.ndhm.gov.in")
                .setValue(patientId);
        return patient;
    }

    public String serializeToJson(Resource resource) {
        return fhirJsonParser.encodeResourceToString(resource);
    }

    public String serializeToXml(Resource resource) {
        return fhirContext.newXmlParser().setPrettyPrint(true)
                .encodeResourceToString(resource);
    }

    public CapabilityStatement generateCapabilityStatement() {
        CapabilityStatement capabilityStatement = new CapabilityStatement();
        capabilityStatement.setId("namaste-icd11-terminology-capability");
        capabilityStatement.setUrl(baseUrl + "/metadata");
        capabilityStatement.setVersion("1.0.0");
        capabilityStatement.setName("NAMASTEIcd11TerminologyCapability");
        capabilityStatement.setTitle("NAMASTE-ICD11 FHIR Terminology Service Capability Statement");
        capabilityStatement.setStatus(Enumerations.PublicationStatus.ACTIVE);
        capabilityStatement.setDate(new Date());
        capabilityStatement.setPublisher("Ministry of AYUSH, Government of India");
        capabilityStatement.setDescription("FHIR Terminology Service supporting NAMASTE and ICD-11 integration");
        capabilityStatement.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);
        capabilityStatement.setFhirVersion(Enumerations.FHIRVersion._4_0_1);
        capabilityStatement.addFormat("json");
        capabilityStatement.addFormat("xml");

        CapabilityStatement.CapabilityStatementRestComponent rest = capabilityStatement.addRest();
        rest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);
        rest.setDocumentation("NAMASTE-ICD11 FHIR Terminology Server");

        // CodeSystem resource
        CapabilityStatement.CapabilityStatementRestResourceComponent codeSystemResource = rest.addResource();
        codeSystemResource.setType("CodeSystem");
        codeSystemResource.addInteraction()
                .setCode(CapabilityStatement.TypeRestfulInteraction.READ);
        codeSystemResource.addInteraction()
                .setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);

        // ConceptMap resource
        CapabilityStatement.CapabilityStatementRestResourceComponent conceptMapResource = rest.addResource();
        conceptMapResource.setType("ConceptMap");
        conceptMapResource.addInteraction()
                .setCode(CapabilityStatement.TypeRestfulInteraction.READ);

        // ValueSet resource
        CapabilityStatement.CapabilityStatementRestResourceComponent valueSetResource = rest.addResource();
        valueSetResource.setType("ValueSet");
        valueSetResource.addInteraction()
                .setCode(CapabilityStatement.TypeRestfulInteraction.READ);

        // Operations
        CapabilityStatement.CapabilityStatementRestResourceOperationComponent translateOp =
                new CapabilityStatement.CapabilityStatementRestResourceOperationComponent();
        translateOp.setName("translate");
        translateOp.setDefinition("http://hl7.org/fhir/OperationDefinition/ConceptMap-translate");
        conceptMapResource.addOperation(translateOp);

        return capabilityStatement;
    }
}