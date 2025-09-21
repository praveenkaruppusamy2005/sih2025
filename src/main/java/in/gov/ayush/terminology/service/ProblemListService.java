package in.gov.ayush.terminology.service;

import ca.uhn.fhir.context.FhirContext;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProblemListService {

    private static final Logger logger = LoggerFactory.getLogger(ProblemListService.class);

    // FhirContext is available but not directly used in this service

    @Autowired
    private NamasteService namasteService;

    @Autowired
    private Icd11Service icd11Service;

    @Autowired
    private ConceptMappingService mappingService;

    @Autowired
    private AuditLogger auditLogger;

    @Value("${fhir.namaste.code-system}")
    private String namasteSystem;

    @Value("${fhir.icd11.tm2-system}")
    private String tm2System;

    @Value("${fhir.icd11.code-system}")
    private String icd11System;

    public Condition createDualCodedCondition(String namasteCode, String patientId,
                                              String clinicalStatus, String verificationStatus,
                                              String onsetDate, String notes) {
        logger.info("Creating dual-coded condition for NAMASTE code: {} and patient: {}", namasteCode, patientId);

        Condition condition = new Condition();
        condition.setId("condition-" + System.currentTimeMillis());
        condition.setSubject(new Reference("Patient/" + patientId));
        
        // Set clinical status
        if (clinicalStatus != null) {
            condition.setClinicalStatus(new CodeableConcept()
                    .addCoding(new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
                            .setCode(clinicalStatus)));
        } else {
        condition.setClinicalStatus(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")
                            .setCode("active")));
        }

        // Set verification status
        if (verificationStatus != null) {
            condition.setVerificationStatus(new CodeableConcept()
                    .addCoding(new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")
                            .setCode(verificationStatus)));
        } else {
            condition.setVerificationStatus(new CodeableConcept()
                    .addCoding(new Coding()
                .setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")
                            .setCode("confirmed")));
        }

        // Create dual coding
        CodeableConcept code = createDualCoding(namasteCode);
        condition.setCode(code);

        // Set onset date
        if (onsetDate != null) {
            try {
                condition.setOnset(new DateTimeType(onsetDate));
            } catch (Exception e) {
                logger.warn("Invalid onset date format: {}", onsetDate);
            }
        }

        // Add notes
        if (notes != null && !notes.trim().isEmpty()) {
            condition.addNote(new Annotation().setText(notes));
        }

        condition.setRecordedDate(new Date());

        auditLogger.logFhirBundleCreation(patientId, 1);
        return condition;
    }

    private CodeableConcept createDualCoding(String namasteCode) {
        CodeableConcept code = new CodeableConcept();

        // Get NAMASTE code
        Optional<NamasteCode> namaste = namasteService.findByCode(namasteCode);
        if (namaste.isPresent()) {
            NamasteCode namasteCodeObj = namaste.get();
            
            // Add NAMASTE coding
            Coding namasteCoding = new Coding()
                    .setSystem(namasteSystem)
                    .setCode(namasteCodeObj.getCode())
                    .setDisplay(namasteCodeObj.getDisplay());
            code.addCoding(namasteCoding);

            // Add mapped ICD-11 TM2 coding
            List<ConceptMapping> tm2Mappings = mappingService.translateNamesteToTm2(namasteCode);
            for (ConceptMapping mapping : tm2Mappings) {
                Optional<Icd11Code> icd11Code = icd11Service.findByCode(mapping.getTargetCode());
                if (icd11Code.isPresent()) {
                    Coding tm2Coding = new Coding()
                            .setSystem(tm2System)
                            .setCode(icd11Code.get().getCode())
                            .setDisplay(icd11Code.get().getTitle());
                    code.addCoding(tm2Coding);
                }
            }

            // Add mapped ICD-11 Biomedicine coding
            List<ConceptMapping> biomedicineMappings = mappingService.translateNamasteToBiomedicine(namasteCode);
            for (ConceptMapping mapping : biomedicineMappings) {
                Optional<Icd11Code> icd11Code = icd11Service.findByCode(mapping.getTargetCode());
                if (icd11Code.isPresent()) {
                    Coding biomedicineCoding = new Coding()
                            .setSystem(icd11System)
                            .setCode(icd11Code.get().getCode())
                            .setDisplay(icd11Code.get().getTitle());
                    code.addCoding(biomedicineCoding);
                }
            }
        }

        return code;
    }

    public Bundle processDualCodedBundle(Bundle bundle) {
        logger.info("Processing dual-coded bundle with {} entries", bundle.getEntry().size());

        Bundle processedBundle = new Bundle();
        processedBundle.setId("processed-" + bundle.getId());
        processedBundle.setType(Bundle.BundleType.COLLECTION);
        processedBundle.setTimestamp(new Date());

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof Condition) {
                    Condition condition = (Condition) entry.getResource();
                    Condition processedCondition = processDualCodedCondition(condition);
                    
                Bundle.BundleEntryComponent newEntry = new Bundle.BundleEntryComponent();
                newEntry.setResource(processedCondition);
                processedBundle.addEntry(newEntry);
            } else {
                // Keep non-Condition resources as-is
                processedBundle.addEntry(entry);
            }
        }

        auditLogger.logFhirBundleCreation("bundle", processedBundle.getEntry().size());
        return processedBundle;
    }

    private Condition processDualCodedCondition(Condition condition) {
        // If condition already has dual coding, return as-is
        if (condition.getCode() != null && condition.getCode().getCoding().size() > 1) {
            return condition;
        }

        // Try to find NAMASTE coding and add dual coding
        for (Coding coding : condition.getCode().getCoding()) {
            if (namasteSystem.equals(coding.getSystem())) {
                CodeableConcept dualCode = createDualCoding(coding.getCode());
                condition.setCode(dualCode);
                break;
            }
        }

        return condition;
    }

    public ValueSet generateDualCodingAutoCompleteValueSet(String term, int limit) {
        logger.info("Generating dual coding autocomplete for term: {}", term);

        ValueSet valueSet = new ValueSet();
        valueSet.setId("dual-coding-autocomplete");
        valueSet.setUrl("http://terminology.ayush.gov.in/ValueSet/dual-coding-autocomplete");
        valueSet.setVersion("1.0");
        valueSet.setName("DualCodingAutoComplete");
        valueSet.setTitle("Dual Coding AutoComplete ValueSet");
        valueSet.setStatus(Enumerations.PublicationStatus.ACTIVE);
        valueSet.setDate(new Date());

        // Get NAMASTE suggestions
        List<NamasteCode> namasteSuggestions = namasteService.getAutoCompleteResults(term, limit);
        
        // Get ICD-11 suggestions
        List<Icd11Code> icd11Suggestions = icd11Service.getAutoCompleteResults(term, limit);

        // Create expansion
        ValueSet.ValueSetExpansionComponent expansion = valueSet.getExpansion();
        expansion.setTimestamp(new Date());

        // Add NAMASTE codes
        for (NamasteCode namaste : namasteSuggestions) {
            ValueSet.ValueSetExpansionContainsComponent contains = expansion.addContains();
            contains.setSystem(namasteSystem);
            contains.setCode(namaste.getCode());
            contains.setDisplay(namaste.getDisplay());
            contains.addDesignation()
                    .setUse(new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/designation-usage")
                            .setCode("preferred"))
                    .setValue("NAMASTE: " + namaste.getDisplay());
        }

        // Add ICD-11 codes
        for (Icd11Code icd11 : icd11Suggestions) {
            ValueSet.ValueSetExpansionContainsComponent contains = expansion.addContains();
            contains.setSystem(icd11.getCodeType() == Icd11Code.CodeType.TM2 ? tm2System : icd11System);
            contains.setCode(icd11.getCode());
            contains.setDisplay(icd11.getTitle());
            contains.addDesignation()
                    .setUse(new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/designation-usage")
                            .setCode("preferred"))
                    .setValue("ICD-11 " + icd11.getCodeType() + ": " + icd11.getTitle());
        }

        return valueSet;
    }

    public Bundle getPatientProblemList(String patientId) {
        logger.info("Retrieving problem list for patient: {}", patientId);

        Bundle problemListBundle = new Bundle();
        problemListBundle.setId("problem-list-" + patientId);
        problemListBundle.setType(Bundle.BundleType.COLLECTION);
        problemListBundle.setTimestamp(new Date());

        // In a real implementation, this would query a database for patient conditions
        // For now, we'll return an empty bundle with proper structure
        problemListBundle.setTotal(0);

        return problemListBundle;
    }

    public Map<String, Object> getCodingSuggestions(String term, int limit) {
        logger.info("Getting coding suggestions for term: {}", term);

        Map<String, Object> suggestions = new HashMap<>();

        // Get NAMASTE suggestions
        List<NamasteCode> namasteSuggestions = namasteService.getAutoCompleteResults(term, limit);
        suggestions.put("namaste", namasteSuggestions.stream()
                .map(this::convertNamasteToMap)
                .collect(Collectors.toList()));

        // Get ICD-11 suggestions
        List<Icd11Code> icd11Suggestions = icd11Service.getAutoCompleteResults(term, limit);
        suggestions.put("icd11", icd11Suggestions.stream()
                .map(this::convertIcd11ToMap)
                .collect(Collectors.toList()));

        // Get mapping suggestions
        List<Map<String, Object>> mappingSuggestions = new ArrayList<>();
        for (NamasteCode namaste : namasteSuggestions) {
            List<ConceptMapping> tm2Mappings = mappingService.translateNamesteToTm2(namaste.getCode());
            List<ConceptMapping> biomedicineMappings = mappingService.translateNamasteToBiomedicine(namaste.getCode());
            
            if (!tm2Mappings.isEmpty() || !biomedicineMappings.isEmpty()) {
                Map<String, Object> mappingSuggestion = new HashMap<>();
                mappingSuggestion.put("namaste", convertNamasteToMap(namaste));
                mappingSuggestion.put("tm2Mappings", tm2Mappings);
                mappingSuggestion.put("biomedicineMappings", biomedicineMappings);
                mappingSuggestions.add(mappingSuggestion);
            }
        }
        suggestions.put("mappings", mappingSuggestions);

        return suggestions;
    }

    public Map<String, Object> validateDualCoding(String namasteCode, String icd11Code, String system) {
        logger.info("Validating dual coding: NAMASTE={}, ICD-11={}, System={}", namasteCode, icd11Code, system);

        Map<String, Object> validation = new HashMap<>();
        validation.put("valid", false);
        validation.put("errors", new ArrayList<>());

        List<String> errors = new ArrayList<>();

        // Validate NAMASTE code
        Optional<NamasteCode> namaste = namasteService.findByCode(namasteCode);
        if (namaste.isEmpty()) {
            errors.add("NAMASTE code not found: " + namasteCode);
        }

        // Validate ICD-11 code
        Optional<Icd11Code> icd11 = icd11Service.findByCode(icd11Code);
        if (icd11.isEmpty()) {
            errors.add("ICD-11 code not found: " + icd11Code);
        }

        // Check if mapping exists
        if (namaste.isPresent() && icd11.isPresent()) {
            String targetSystem = "TM2".equals(system) ? tm2System : icd11System;
            List<ConceptMapping> mappings = mappingService.findMappingsForCode(namasteCode, namasteSystem);
            
            boolean mappingExists = mappings.stream()
                    .anyMatch(m -> icd11Code.equals(m.getTargetCode()) && targetSystem.equals(m.getTargetSystem()));
            
            if (!mappingExists) {
                errors.add("No mapping found between NAMASTE code " + namasteCode + " and ICD-11 code " + icd11Code);
            } else {
                validation.put("valid", true);
                validation.put("mapping", mappings.stream()
                        .filter(m -> icd11Code.equals(m.getTargetCode()) && targetSystem.equals(m.getTargetSystem()))
                        .findFirst()
                        .orElse(null));
            }
        }

        validation.put("errors", errors);
        return validation;
    }

    private Map<String, Object> convertNamasteToMap(NamasteCode namaste) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", namaste.getCode());
        map.put("display", namaste.getDisplay());
        map.put("definition", namaste.getDefinition());
        map.put("system", namaste.getSystem());
        map.put("category", namaste.getCategory());
        map.put("whoTerminologyCode", namaste.getWhoTerminologyCode());
        map.put("icd11Tm2Code", namaste.getIcd11Tm2Code());
        map.put("icd11BiomedicineCode", namaste.getIcd11BiomedicineCode());
        return map;
    }

    private Map<String, Object> convertIcd11ToMap(Icd11Code icd11) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", icd11.getCode());
        map.put("title", icd11.getTitle());
        map.put("definition", icd11.getDefinition());
        map.put("codeType", icd11.getCodeType());
        map.put("parent", icd11.getParent());
        map.put("chapter", icd11.getChapter());
        return map;
    }
}