package in.gov.ayush.terminology.controller;

import in.gov.ayush.terminology.model.ConceptMapping;
import in.gov.ayush.terminology.model.Icd11Code;
import in.gov.ayush.terminology.model.NamasteCode;
import in.gov.ayush.terminology.service.ConceptMappingService;
import in.gov.ayush.terminology.service.Icd11Service;
import in.gov.ayush.terminology.service.NamasteService;
import in.gov.ayush.terminology.utils.AuditLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/terminology")
@Tag(name = "Terminology API", description = "NAMASTE and ICD-11 terminology operations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TerminologyController {

    @Autowired
    private NamasteService namasteService;

    @Autowired
    private Icd11Service icd11Service;

    @Autowired
    private ConceptMappingService mappingService;

    @Autowired
    private AuditLogger auditLogger;

    // NAMASTE Endpoints
    @Operation(summary = "Search NAMASTE codes",
            description = "Search for NAMASTE codes by term with pagination")
    @GetMapping("/namaste/search")
    public ResponseEntity<Page<NamasteCode>> searchNamasteCodes(
            @Parameter(description = "Search term") @RequestParam String term,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/namaste/search", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        Page<NamasteCode> results = namasteService.searchCodes(term, page, size);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get NAMASTE code by code",
            description = "Retrieve a specific NAMASTE code by its identifier")
    @GetMapping("/namaste/code/{code}")
    public ResponseEntity<NamasteCode> getNamasteCode(
            @Parameter(description = "NAMASTE code") @PathVariable String code,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/namaste/code/" + code, "GET",
                authentication != null ? authentication.getName() : "anonymous");

        Optional<NamasteCode> namasteCode = namasteService.findByCode(code);
        return namasteCode.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Auto-complete NAMASTE codes",
            description = "Get auto-complete suggestions for NAMASTE codes")
    @GetMapping("/namaste/autocomplete")
    public ResponseEntity<List<NamasteCode>> autoCompleteNamaste(
            @Parameter(description = "Search term") @RequestParam String term,
            @Parameter(description = "Result limit") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/namaste/autocomplete", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        List<NamasteCode> results = namasteService.getAutoCompleteResults(term, limit);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get NAMASTE codes by system",
            description = "Retrieve NAMASTE codes filtered by traditional medicine system")
    @GetMapping("/namaste/system/{system}")
    public ResponseEntity<List<NamasteCode>> getNamasteBySystem(
            @Parameter(description = "Traditional medicine system")
            @PathVariable NamasteCode.TraditionalSystem system,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/namaste/system/" + system, "GET",
                authentication != null ? authentication.getName() : "anonymous");

        List<NamasteCode> results = namasteService.findBySystem(system);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get categories by system",
            description = "Get available categories for a traditional medicine system")
    @GetMapping("/namaste/categories/{system}")
    public ResponseEntity<List<String>> getCategoriesBySystem(
            @Parameter(description = "Traditional medicine system")
            @PathVariable NamasteCode.TraditionalSystem system,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/namaste/categories/" + system, "GET",
                authentication != null ? authentication.getName() : "anonymous");

        List<String> categories = namasteService.getCategoriesBySystem(system);
        return ResponseEntity.ok(categories);
    }

    // ICD-11 Endpoints
    @Operation(summary = "Search ICD-11 codes",
            description = "Search for ICD-11 codes by term with pagination")
    @GetMapping("/icd11/search")
    public ResponseEntity<Page<Icd11Code>> searchIcd11Codes(
            @Parameter(description = "Search term") @RequestParam String term,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/icd11/search", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        Page<Icd11Code> results = icd11Service.searchCodes(term, page, size);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get ICD-11 code by code",
            description = "Retrieve a specific ICD-11 code by its identifier")
    @GetMapping("/icd11/code/{code}")
    public ResponseEntity<Icd11Code> getIcd11Code(
            @Parameter(description = "ICD-11 code") @PathVariable String code,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/icd11/code/" + code, "GET",
                authentication != null ? authentication.getName() : "anonymous");

        Optional<Icd11Code> icd11Code = icd11Service.findByCode(code);
        return icd11Code.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Auto-complete ICD-11 codes",
            description = "Get auto-complete suggestions for ICD-11 codes")
    @GetMapping("/icd11/autocomplete")
    public ResponseEntity<List<Icd11Code>> autoCompleteIcd11(
            @Parameter(description = "Search term") @RequestParam String term,
            @Parameter(description = "Result limit") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/icd11/autocomplete", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        List<Icd11Code> results = icd11Service.getAutoCompleteResults(term, limit);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Get ICD-11 codes by type",
            description = "Retrieve ICD-11 codes filtered by code type (TM2 or Biomedicine)")
    @GetMapping("/icd11/type/{type}")
    public ResponseEntity<List<Icd11Code>> getIcd11ByType(
            @Parameter(description = "Code type") @PathVariable Icd11Code.CodeType type,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/icd11/type/" + type, "GET",
                authentication != null ? authentication.getName() : "anonymous");

        List<Icd11Code> results = icd11Service.findByCodeType(type);
        return ResponseEntity.ok(results);
    }

    // Translation Endpoints
    @Operation(summary = "Translate NAMASTE to TM2",
            description = "Translate a NAMASTE code to ICD-11 TM2 codes")
    @GetMapping("/translate/namaste-to-tm2/{code}")
    public ResponseEntity<List<ConceptMapping>> translateNamesteToTm2(
            @Parameter(description = "NAMASTE code") @PathVariable String code,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/translate/namaste-to-tm2/" + code, "GET",
                authentication != null ? authentication.getName() : "anonymous");

        List<ConceptMapping> mappings = mappingService.translateNamesteToTm2(code);
        return ResponseEntity.ok(mappings);
    }

    @Operation(summary = "Translate TM2 to NAMASTE",
            description = "Translate an ICD-11 TM2 code to NAMASTE codes")
    @GetMapping("/translate/tm2-to-namaste/{code}")
    public ResponseEntity<List<ConceptMapping>> translateTm2ToNamaste(
            @Parameter(description = "ICD-11 TM2 code") @PathVariable String code,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/translate/tm2-to-namaste/" + code, "GET",
                authentication != null ? authentication.getName() : "anonymous");

        List<ConceptMapping> mappings = mappingService.translateTm2ToNamaste(code);
        return ResponseEntity.ok(mappings);
    }

    @Operation(summary = "Translate NAMASTE to Biomedicine",
            description = "Translate a NAMASTE code to ICD-11 Biomedicine codes")
    @GetMapping("/translate/namaste-to-biomedicine/{code}")
    public ResponseEntity<List<ConceptMapping>> translateNamasteToBiomedicine(
            @Parameter(description = "NAMASTE code") @PathVariable String code,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/translate/namaste-to-biomedicine/" + code, "GET",
                authentication != null ? authentication.getName() : "anonymous");

        List<ConceptMapping> mappings = mappingService.translateNamasteToBiomedicine(code);
        return ResponseEntity.ok(mappings);
    }

    // Mapping Management Endpoints
    @Operation(summary = "Create concept mapping",
            description = "Create a new concept mapping between code systems")
    @PostMapping("/mapping")
    public ResponseEntity<ConceptMapping> createMapping(
            @Valid @RequestBody CreateMappingRequest request,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/mapping", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        ConceptMapping mapping = mappingService.createMapping(
                request.getSourceCode(), request.getSourceSystem(),
                request.getTargetCode(), request.getTargetSystem(),
                request.getEquivalence());

        return ResponseEntity.ok(mapping);
    }

    @Operation(summary = "Get mappings for code",
            description = "Get all mappings for a specific code")
    @GetMapping("/mapping/{system}/{code}")
    public ResponseEntity<List<ConceptMapping>> getMappingsForCode(
            @Parameter(description = "Code system") @PathVariable String system,
            @Parameter(description = "Code") @PathVariable String code,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/mapping/" + system + "/" + code, "GET",
                authentication != null ? authentication.getName() : "anonymous");

        List<ConceptMapping> mappings = mappingService.findMappingsForCode(code, system);
        return ResponseEntity.ok(mappings);
    }

    @Operation(summary = "Delete mapping",
            description = "Delete a concept mapping")
    @DeleteMapping("/mapping/{id}")
    public ResponseEntity<Void> deleteMapping(
            @Parameter(description = "Mapping ID") @PathVariable Long id,
            Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/mapping/" + id, "DELETE",
                authentication != null ? authentication.getName() : "anonymous");

        mappingService.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }

    // Statistics Endpoints
    @Operation(summary = "Get system statistics",
            description = "Get statistics about the terminology systems")
    @GetMapping("/stats")
    public ResponseEntity<TerminologyStats> getStats(Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/stats", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        TerminologyStats stats = new TerminologyStats();
        stats.setNamasteCodeCount(namasteService.getCodeCount());
        stats.setIcd11CodeCount(icd11Service.getCodeCount());
        stats.setMappingCount(mappingService.getMappingCount());
        stats.setAyurvedaCount(namasteService.getCodeCountBySystem(NamasteCode.TraditionalSystem.AYURVEDA));
        stats.setSiddhaCount(namasteService.getCodeCountBySystem(NamasteCode.TraditionalSystem.SIDDHA));
        stats.setUnaniCount(namasteService.getCodeCountBySystem(NamasteCode.TraditionalSystem.UNANI));
        stats.setTm2Count(icd11Service.getCodeCountByType(Icd11Code.CodeType.TM2));
        stats.setBiomedicineCount(icd11Service.getCodeCountByType(Icd11Code.CodeType.BIOMEDICINE));

        return ResponseEntity.ok(stats);
    }

    // Administrative Endpoints
    @Operation(summary = "Generate automatic mappings",
            description = "Generate automatic mappings based on existing WHO terminology codes")
    @PostMapping("/admin/generate-mappings")
    public ResponseEntity<String> generateMappings(Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/admin/generate-mappings", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        mappingService.generateAutomaticMappings();
        return ResponseEntity.ok("Automatic mapping generation initiated");
    }

    @Operation(summary = "Reload NAMASTE data",
            description = "Reload NAMASTE codes from CSV file")
    @PostMapping("/admin/reload-namaste")
    public ResponseEntity<String> reloadNamesteData(Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/admin/reload-namaste", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        namasteService.loadNamasteCodesFromCsv();
        return ResponseEntity.ok("NAMASTE data reload completed");
    }

    @Operation(summary = "Sync ICD-11 data",
            description = "Synchronize ICD-11 data from WHO API")
    @PostMapping("/admin/sync-icd11")
    public ResponseEntity<String> syncIcd11Data(Authentication authentication) {

        auditLogger.logApiAccess("/api/terminology/admin/sync-icd11", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        icd11Service.syncIcd11Data();
        return ResponseEntity.ok("ICD-11 data synchronization initiated");
    }

    // Request/Response DTOs
    public static class CreateMappingRequest {
        @jakarta.validation.constraints.NotBlank
        private String sourceCode;

        @jakarta.validation.constraints.NotBlank
        private String sourceSystem;

        @jakarta.validation.constraints.NotBlank
        private String targetCode;

        @jakarta.validation.constraints.NotBlank
        private String targetSystem;

        @jakarta.validation.constraints.NotNull
        private ConceptMapping.MappingEquivalence equivalence;

        private String comment;
        private Double confidenceScore;

        // Getters and Setters
        public String getSourceCode() { return sourceCode; }
        public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

        public String getSourceSystem() { return sourceSystem; }
        public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }

        public String getTargetCode() { return targetCode; }
        public void setTargetCode(String targetCode) { this.targetCode = targetCode; }

        public String getTargetSystem() { return targetSystem; }
        public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }

        public ConceptMapping.MappingEquivalence getEquivalence() { return equivalence; }
        public void setEquivalence(ConceptMapping.MappingEquivalence equivalence) {
            this.equivalence = equivalence;
        }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double confidenceScore) {
            this.confidenceScore = confidenceScore;
        }
    }

    public static class TerminologyStats {
        private long namasteCodeCount;
        private long icd11CodeCount;
        private long mappingCount;
        private long ayurvedaCount;
        private long siddhaCount;
        private long unaniCount;
        private long tm2Count;
        private long biomedicineCount;

        // Getters and Setters
        public long getNamasteCodeCount() { return namasteCodeCount; }
        public void setNamasteCodeCount(long namasteCodeCount) {
            this.namasteCodeCount = namasteCodeCount;
        }

        public long getIcd11CodeCount() { return icd11CodeCount; }
        public void setIcd11CodeCount(long icd11CodeCount) { this.icd11CodeCount = icd11CodeCount; }

        public long getMappingCount() { return mappingCount; }
        public void setMappingCount(long mappingCount) { this.mappingCount = mappingCount; }

        public long getAyurvedaCount() { return ayurvedaCount; }
        public void setAyurvedaCount(long ayurvedaCount) { this.ayurvedaCount = ayurvedaCount; }

        public long getSiddhaCount() { return siddhaCount; }
        public void setSiddhaCount(long siddhaCount) { this.siddhaCount = siddhaCount; }

        public long getUnaniCount() { return unaniCount; }
        public void setUnaniCount(long unaniCount) { this.unaniCount = unaniCount; }

        public long getTm2Count() { return tm2Count; }
        public void setTm2Count(long tm2Count) { this.tm2Count = tm2Count; }

        public long getBiomedicineCount() { return biomedicineCount; }
        public void setBiomedicineCount(long biomedicineCount) {
            this.biomedicineCount = biomedicineCount;
        }
    }
}