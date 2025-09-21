package in.gov.ayush.terminology.service;


import in.gov.ayush.terminology.model.Icd11Code;
import in.gov.ayush.terminology.model.Icd11Code.CodeType;
import in.gov.ayush.terminology.repository.Icd11Repository;
import in.gov.ayush.terminology.utils.AuditLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Service
@Transactional
public class Icd11Service {

    private static final Logger logger = LoggerFactory.getLogger(Icd11Service.class);

    @Autowired
    private Icd11Repository icd11Repository;

    @Autowired
    private AuditLogger auditLogger;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${icd11.api.base-url}")
    private String baseUrl;

    @Value("${icd11.api.client-id}")
    private String clientId;

    @Value("${icd11.api.client-secret}")
    private String clientSecret;

    @Value("${icd11.api.token-url}")
    private String tokenUrl;

    private String accessToken;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void initialize() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        initializeAccessToken();
    }

    private void initializeAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId, clientSecret);
            headers.add("Content-Type", "application/x-www-form-urlencoded");
            headers.set("API-Version", "v2");

            String requestBody = "grant_type=client_credentials&scope=icdapi_access";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode tokenResponse = objectMapper.readTree(response.getBody());
                JsonNode tokenNode = tokenResponse.get("access_token");
                if (tokenNode != null) {
                    this.accessToken = tokenNode.asText();
                    logger.info("Successfully obtained ICD-11 API access token");
                } else {
                    logger.error("Access token not found in response");
                }
            } else {
                logger.error("Failed to obtain token: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Failed to obtain ICD-11 API access token", e);
        }
        
        // Load sample ICD-11 data for development
        loadSampleIcd11Data();
    }
    
    private void loadSampleIcd11Data() {
        try {
            logger.info("Loading sample ICD-11 data from CSV...");
            Resource resource = new ClassPathResource("sample-icd11.csv");
            
            if (!resource.exists()) {
                logger.warn("Sample ICD-11 CSV file not found");
                return;
            }
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                String line;
                boolean firstLine = true;
                int count = 0;
                
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue; // Skip header
                    }
                    
                    String[] fields = line.split(",");
                    if (fields.length >= 4) {
                        String code = fields[0].trim();
                        String title = fields[1].trim();
                        String definition = fields[2].trim();
                        String codeTypeStr = fields[3].trim();
                        String chapter = fields.length > 4 ? fields[4].trim() : null;
                        String parent = fields.length > 5 ? fields[5].trim() : null;
                        
                        CodeType codeType = CodeType.valueOf(codeTypeStr);
                        
                        Optional<Icd11Code> existing = icd11Repository.findByCode(code);
                        Icd11Code icd11Code = existing.orElseGet(() -> new Icd11Code(code, title, codeType));
                        
                        icd11Code.setTitle(title);
                        icd11Code.setDefinition(definition);
                        icd11Code.setChapter(chapter);
                        if (parent != null && !parent.isEmpty()) {
                            icd11Code.setParent(parent);
                        }
                        
                        icd11Repository.save(icd11Code);
                        count++;
                    }
                }
                
                logger.info("Loaded {} ICD-11 codes from CSV", count);
                auditLogger.logDataLoad("ICD11_CSV", count);
            }
        } catch (Exception e) {
            logger.error("Failed to load sample ICD-11 data", e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void refreshAccessToken() {
        initializeAccessToken();
    }

    @Async
    @Scheduled(fixedRate = 86_400_000)
    public void syncIcd11Data() {
        try {
            logger.info("Starting ICD-11 data synchronization...");
            syncTm2Codes();
            syncBiomedicineCodes();
            logger.info("ICD-11 data synchronization completed");
        } catch (Exception e) {
            logger.error("Failed to synchronize ICD-11 data", e);
        }
    }

    private void syncTm2Codes() {
        try {
            // WHO ICD-11 TM2 endpoint - Traditional Medicine Module 2
            String tm2Url = "https://id.who.int/icd/release/11/2019-04/mms/tm2";
            syncCodesFromEndpoint(tm2Url, CodeType.TM2);
        } catch (Exception e) {
            logger.error("Failed to sync TM2 codes", e);
        }
    }

    private void syncBiomedicineCodes() {
        try {
            // WHO ICD-11 Biomedicine endpoint - Main ICD-11 classification
            String biomedicineUrl = "https://id.who.int/icd/release/11/2019-04/mms";
            syncCodesFromEndpoint(biomedicineUrl, CodeType.BIOMEDICINE);
        } catch (Exception e) {
            logger.error("Failed to sync Biomedicine codes", e);
        }
    }

    private void syncCodesFromEndpoint(String endpoint, CodeType codeType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.add("Accept", "application/json");
            headers.add("Accept-Language", "en");
            headers.set("API-Version", "v2");

            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                processIcd11Response(response.getBody(), codeType);
            } else {
                logger.error("Endpoint {} returned status {}", endpoint, response.getStatusCode());
            }
        } catch (HttpClientErrorException.NotFound nf) {
            logger.error("Endpoint {} not found (404)", endpoint, nf);
        } catch (Exception e) {
            logger.error("Failed to sync codes from endpoint: " + endpoint, e);
        }
    }

    private void processIcd11Response(String jsonResponse, CodeType codeType) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            if (rootNode.has("child") && rootNode.get("child").isArray()) {
                for (JsonNode child : rootNode.get("child")) {
                    processIcd11Node(child, codeType, null);
                }
            }
            auditLogger.logDataSync("ICD11_" + codeType.name(), "SUCCESS");
        } catch (Exception e) {
            logger.error("Failed to process ICD-11 response", e);
            auditLogger.logDataSync("ICD11_" + codeType.name(), "FAILED");
        }
    }

    private void processIcd11Node(JsonNode node, CodeType codeType, String parentCode) {
        try {
            if (node == null) return;

            JsonNode idNode = node.get("@id");
            if (idNode == null || idNode.asText().isEmpty()) {
                logger.warn("Skipping node with missing @id: {}", node.toString());
                return;
            }

            String code = extractCodeFromUrl(idNode.asText());
            String title = node.path("title").path("@value").asText("");

            Optional<Icd11Code> existing = icd11Repository.findByCode(code);
            Icd11Code icd11Code = existing.orElseGet(() -> new Icd11Code(code, title, codeType));

            icd11Code.setTitle(title);

            if (parentCode != null) {
                icd11Code.setParent(parentCode);
            }

            icd11Code.setDefinition(node.path("definition").path("@value").asText(null));
            icd11Code.setFoundationUri(node.path("foundationReference").asText(null));

            icd11Repository.save(icd11Code);

            if (node.has("child") && node.get("child").isArray()) {
                for (JsonNode child : node.get("child")) {
                    processIcd11Node(child, codeType, code);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to process ICD-11 node", e);
        }
    }

    private String extractCodeFromUrl(String url) {
        if (url == null) return null;
        int idx = url.lastIndexOf('/');
        return (idx >= 0) ? url.substring(idx + 1) : url;
    }

    public Optional<Icd11Code> findByCode(String code) {
        auditLogger.logCodeLookup("ICD11", code);
        return icd11Repository.findByCode(code);
    }

    public Page<Icd11Code> searchCodes(String term, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Icd11Code> results = icd11Repository.searchByTerm(term, pageable);
        auditLogger.logSearch("ICD11", term, results.getTotalElements());
        return results;
    }

    public List<Icd11Code> getAutoCompleteResults(String term, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return icd11Repository.findForAutoComplete(term, pageable);
    }

    public List<Icd11Code> findByCodeType(CodeType codeType) {
        return icd11Repository.findByCodeType(codeType);
    }

    public List<Icd11Code> findByChapter(String chapter) {
        return icd11Repository.findByChapter(chapter);
    }

    public List<String> getChaptersByCodeType(CodeType codeType) {
        return icd11Repository.findChaptersByCodeType(codeType);
    }

    public Icd11Code save(Icd11Code code) {
        Icd11Code saved = icd11Repository.save(code);
        auditLogger.logCodeUpdate("ICD11", code.getCode(), "SAVE");
        return saved;
    }

    public long getCodeCount() {
        return icd11Repository.count();
    }

    public long getCodeCountByType(CodeType codeType) {
        return icd11Repository.findByCodeType(codeType).size();
    }
}