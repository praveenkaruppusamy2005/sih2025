package in.gov.ayush.terminology.service;


import in.gov.ayush.terminology.model.ConceptMapping;
import in.gov.ayush.terminology.model.ConceptMapping.*;
import in.gov.ayush.terminology.model.NamasteCode;
import in.gov.ayush.terminology.repository.ConceptMappingRepository;
import in.gov.ayush.terminology.utils.AuditLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConceptMappingService {

    private static final Logger logger = LoggerFactory.getLogger(ConceptMappingService.class);

    @Autowired
    private ConceptMappingRepository mappingRepository;

    @Autowired
    private NamasteService namasteService;

    @Autowired
    private Icd11Service icd11Service;

    @Autowired
    private AuditLogger auditLogger;

    @Value("${fhir.namaste.code-system}")
    private String namasteSystem;

    @Value("${fhir.icd11.tm2-system}")
    private String tm2System;

    @Value("${fhir.icd11.code-system}")
    private String icd11System;

    public ConceptMapping createMapping(String sourceCode, String sourceSystem,
                                        String targetCode, String targetSystem,
                                        MappingEquivalence equivalence) {
        ConceptMapping mapping = new ConceptMapping(sourceCode, sourceSystem,
                targetCode, targetSystem, equivalence);
        ConceptMapping saved = mappingRepository.save(mapping);
        auditLogger.logMappingCreation(sourceCode, targetCode, equivalence.name());
        return saved;
    }

    public List<ConceptMapping> findMappingsForCode(String code, String system) {
        return mappingRepository.findMappingsForCode(code, system);
    }

    public List<ConceptMapping> translateNamesteToTm2(String namasteCode) {
        auditLogger.logTranslation("NAMASTE_TO_TM2", namasteCode);
        return mappingRepository.findBySourceCodeAndSourceSystem(namasteCode, namasteSystem)
                .stream()
                .filter(m -> tm2System.equals(m.getTargetSystem()))
                .toList();
    }

    public List<ConceptMapping> translateTm2ToNamaste(String tm2Code) {
        auditLogger.logTranslation("TM2_TO_NAMASTE", tm2Code);
        return mappingRepository.findByTargetCodeAndTargetSystem(tm2Code, tm2System)
                .stream()
                .filter(m -> namasteSystem.equals(m.getSourceSystem()))
                .toList();
    }

    public List<ConceptMapping> translateNamasteToBiomedicine(String namasteCode) {
        auditLogger.logTranslation("NAMASTE_TO_BIOMEDICINE", namasteCode);
        return mappingRepository.findBySourceCodeAndSourceSystem(namasteCode, namasteSystem)
                .stream()
                .filter(m -> icd11System.equals(m.getTargetSystem()))
                .toList();
    }

    public void generateAutomaticMappings() {
        logger.info("Starting automatic mapping generation...");

        try {
            // Generate mappings for codes with WHO terminology mappings
            List<NamasteCode> namasteWithWho = namasteService.findCodesWithWhoMapping();
            for (NamasteCode namaste : namasteWithWho) {
                if (namaste.getIcd11Tm2Code() != null) {
                    createMappingIfNotExists(namaste.getCode(), namasteSystem,
                            namaste.getIcd11Tm2Code(), tm2System,
                            MappingEquivalence.EQUIVALENT);
                }

                if (namaste.getIcd11BiomedicineCode() != null) {
                    createMappingIfNotExists(namaste.getCode(), namasteSystem,
                            namaste.getIcd11BiomedicineCode(), icd11System,
                            MappingEquivalence.RELATEDTO);
                }
            }

            auditLogger.logAutomaticMapping("SUCCESS", namasteWithWho.size());
            logger.info("Automatic mapping generation completed for {} codes",
                    namasteWithWho.size());

        } catch (Exception e) {
            logger.error("Failed to generate automatic mappings", e);
            auditLogger.logAutomaticMapping("FAILED", 0);
        }
    }

    private void createMappingIfNotExists(String sourceCode, String sourceSystem,
                                          String targetCode, String targetSystem,
                                          MappingEquivalence equivalence) {
        Optional<ConceptMapping> existing = mappingRepository
                .findBySourceCodeAndSourceSystemAndTargetSystem(sourceCode, sourceSystem, targetSystem);

        if (existing.isEmpty()) {
            createMapping(sourceCode, sourceSystem, targetCode, targetSystem, equivalence);
        }
    }

    public List<ConceptMapping> getAllMappings() {
        return mappingRepository.findAll();
    }

    public List<ConceptMapping> getMappingsByEquivalence(MappingEquivalence equivalence) {
        return mappingRepository.findByEquivalence(equivalence);
    }

    public List<ConceptMapping> getMappingsBetweenSystems(String sourceSystem, String targetSystem) {
        return mappingRepository.findBySourceAndTargetSystem(sourceSystem, targetSystem);
    }

    public void deleteMapping(Long mappingId) {
        mappingRepository.deleteById(mappingId);
        auditLogger.logMappingDeletion(mappingId.toString());
    }

    public long getMappingCount() {
        return mappingRepository.count();
    }

    public long getMappingCountBetweenSystems(String sourceSystem, String targetSystem) {
        return mappingRepository.findBySourceAndTargetSystem(sourceSystem, targetSystem).size();
    }
}