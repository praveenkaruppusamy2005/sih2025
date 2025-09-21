package in.gov.ayush.terminology.service;


import in.gov.ayush.terminology.model.NamasteCode;
import in.gov.ayush.terminology.repository.NamasteRepository;
import in.gov.ayush.terminology.utils.AuditLogger;
import in.gov.ayush.terminology.utils.CsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NamasteService {

    private static final Logger logger = LoggerFactory.getLogger(NamasteService.class);

    @Autowired
    private NamasteRepository namasteRepository;

    @Autowired
    private CsvParser csvParser;

    @Autowired
    private AuditLogger auditLogger;

    @Value("${csv.namaste-file}")
    private String namasteFilePath;

    @PostConstruct
    public void initializeData() {
        try {
            if (namasteRepository.count() == 0) {
                logger.info("Initializing NAMASTE data from CSV...");
                loadNamasteCodesFromCsv();
                logger.info("NAMASTE data initialization completed.");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize NAMASTE data", e);
        }
    }

    public void loadNamasteCodesFromCsv() {
        try {
            List<NamasteCode> codes = csvParser.parseNamasteCsv(namasteFilePath);
            namasteRepository.saveAll(codes);
            auditLogger.logDataLoad("NAMASTE_CSV", codes.size());
            logger.info("Loaded {} NAMASTE codes from CSV", codes.size());
        } catch (Exception e) {
            logger.error("Failed to load NAMASTE codes from CSV", e);
            throw new RuntimeException("Failed to load NAMASTE data", e);
        }
    }

    public Optional<NamasteCode> findByCode(String code) {
        auditLogger.logCodeLookup("NAMASTE", code);
        return namasteRepository.findByCode(code);
    }

    public Page<NamasteCode> searchCodes(String term, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NamasteCode> results = namasteRepository.searchByTerm(term, pageable);
        auditLogger.logSearch("NAMASTE", term, results.getTotalElements());
        return results;
    }

    public List<NamasteCode> getAutoCompleteResults(String term, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return namasteRepository.findForAutoComplete(term, pageable);
    }

    public List<NamasteCode> findBySystem(NamasteCode.TraditionalSystem system) {
        return namasteRepository.findBySystem(system);
    }

    public List<NamasteCode> findByCategory(String category) {
        return namasteRepository.findByCategory(category);
    }

    public List<String> getCategoriesBySystem(NamasteCode.TraditionalSystem system) {
        return namasteRepository.findCategoriesBySystem(system);
    }

    public List<NamasteCode> findCodesWithWhoMapping() {
        return namasteRepository.findByWhoTerminologyCodeIsNotNull();
    }

    public List<NamasteCode> findCodesWithIcd11Tm2Mapping() {
        return namasteRepository.findByIcd11Tm2CodeIsNotNull();
    }

    public List<NamasteCode> findCodesWithBiomedicineMapping() {
        return namasteRepository.findByIcd11BiomedicineCodeIsNotNull();
    }

    public NamasteCode save(NamasteCode code) {
        NamasteCode saved = namasteRepository.save(code);
        auditLogger.logCodeUpdate("NAMASTE", code.getCode(), "SAVE");
        return saved;
    }

    public void delete(String code) {
        Optional<NamasteCode> existing = namasteRepository.findByCode(code);
        if (existing.isPresent()) {
            namasteRepository.delete(existing.get());
            auditLogger.logCodeUpdate("NAMASTE", code, "DELETE");
        }
    }

    public long getCodeCount() {
        return namasteRepository.count();
    }

    public long getCodeCountBySystem(NamasteCode.TraditionalSystem system) {
        return namasteRepository.findBySystem(system).size();
    }
}
