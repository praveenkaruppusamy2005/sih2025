package in.gov.ayush.terminology.utils;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void logCodeLookup(String system, String code) {
        MDC.put("operation", "CODE_LOOKUP");
        MDC.put("system", system);
        MDC.put("code", code);
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("Code lookup: system={}, code={}", system, code);

        MDC.clear();
    }

    public void logSearch(String system, String term, long resultCount) {
        MDC.put("operation", "SEARCH");
        MDC.put("system", system);
        MDC.put("term", term);
        MDC.put("resultCount", String.valueOf(resultCount));
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("Search performed: system={}, term={}, results={}",
                system, term, resultCount);

        MDC.clear();
    }

    public void logTranslation(String operation, String code) {
        MDC.put("operation", "TRANSLATION");
        MDC.put("translationType", operation);
        MDC.put("code", code);
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("Code translation: type={}, code={}", operation, code);

        MDC.clear();
    }

    public void logDataLoad(String source, int recordCount) {
        MDC.put("operation", "DATA_LOAD");
        MDC.put("source", source);
        MDC.put("recordCount", String.valueOf(recordCount));
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("Data loaded: source={}, records={}", source, recordCount);

        MDC.clear();
    }

    public void logDataSync(String system, String status) {
        MDC.put("operation", "DATA_SYNC");
        MDC.put("system", system);
        MDC.put("status", status);
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("Data sync: system={}, status={}", system, status);

        MDC.clear();
    }

    public void logMappingCreation(String sourceCode, String targetCode, String equivalence) {
        MDC.put("operation", "MAPPING_CREATION");
        MDC.put("sourceCode", sourceCode);
        MDC.put("targetCode", targetCode);
        MDC.put("equivalence", equivalence);
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("Mapping created: {}→{} ({})", sourceCode, targetCode, equivalence);

        MDC.clear();
    }

    public void logMappingDeletion(String mappingId) {
        MDC.put("operation", "MAPPING_DELETION");
        MDC.put("mappingId", mappingId);
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("Mapping deleted: id={}", mappingId);

        MDC.clear();
    }

    public void logAutomaticMapping(String status, int mappingCount) {
        MDC.put("operation", "AUTOMATIC_MAPPING");
        MDC.put("status", status);
        MDC.put("mappingCount", String.valueOf(mappingCount));
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("Automatic mapping: status={}, count={}", status, mappingCount);

        MDC.clear();
    }

    public void logCodeUpdate(String system, String code, String action) {
        MDC.put("operation", "CODE_UPDATE");
        MDC.put("system", system);
        MDC.put("code", code);
        MDC.put("action", action);
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("Code updated: system={}, code={}, action={}", system, code, action);

        MDC.clear();
    }

    public void logFhirResourceGeneration(String resourceType, String system, int count) {
        MDC.put("operation", "FHIR_GENERATION");
        MDC.put("resourceType", resourceType);
        MDC.put("system", system);
        MDC.put("count", String.valueOf(count));
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("FHIR resource generated: type={}, system={}, count={}",
                resourceType, system, count);

        MDC.clear();
    }

    public void logFhirBundleCreation(String patientId, int conditionCount) {
        MDC.put("operation", "FHIR_BUNDLE_CREATION");
        MDC.put("patientId", patientId);
        MDC.put("conditionCount", String.valueOf(conditionCount));
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("FHIR Bundle created: patient={}, conditions={}",
                patientId, conditionCount);

        MDC.clear();
    }

    public void logApiAccess(String endpoint, String method, String userId) {
        MDC.put("operation", "API_ACCESS");
        MDC.put("endpoint", endpoint);
        MDC.put("method", method);
        MDC.put("userId", userId);
        MDC.put("timestamp", LocalDateTime.now().format(formatter));

        auditLog.info("API accessed: {} {} by user {}", method, endpoint, userId);

        MDC.clear();
    }
}