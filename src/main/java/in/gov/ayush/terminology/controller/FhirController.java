package in.gov.ayush.terminology.controller;

import ca.uhn.fhir.context.FhirContext;

import in.gov.ayush.terminology.model.NamasteCode;
import in.gov.ayush.terminology.service.FhirService;
import in.gov.ayush.terminology.utils.AuditLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/fhir")
@Tag(name = "FHIR API", description = "FHIR R4 compliant terminology server endpoints")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FhirController {

    @Autowired
    private FhirService fhirService;

    @Autowired
    private FhirContext fhirContext;

    @Autowired
    private AuditLogger auditLogger;

    @Operation(summary = "Get server capability statement",
            description = "Returns the FHIR CapabilityStatement for this terminology server")
    @GetMapping("/metadata")
    public ResponseEntity<String> getCapabilityStatement(
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/metadata", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        CapabilityStatement capabilityStatement = fhirService.generateCapabilityStatement();

        String response;
        MediaType mediaType;

        if ("xml".equalsIgnoreCase(_format)) {
            response = fhirService.serializeToXml(capabilityStatement);
            mediaType = MediaType.APPLICATION_XML;
        } else {
            response = fhirService.serializeToJson(capabilityStatement);
            mediaType = MediaType.APPLICATION_JSON;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add("Cache-Control", "no-cache");

        return ResponseEntity.ok().headers(headers).body(response);
    }

    @Operation(summary = "Get NAMASTE CodeSystem",
            description = "Returns the FHIR CodeSystem resource for NAMASTE codes")
    @GetMapping("/CodeSystem/namaste-codes")
    public ResponseEntity<String> getNamasteCodeSystem(
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/CodeSystem/namaste-codes", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        CodeSystem codeSystem = fhirService.generateNamasteCodeSystem();

        String response;
        MediaType mediaType;

        if ("xml".equalsIgnoreCase(_format)) {
            response = fhirService.serializeToXml(codeSystem);
            mediaType = MediaType.APPLICATION_XML;
        } else {
            response = fhirService.serializeToJson(codeSystem);
            mediaType = MediaType.APPLICATION_JSON;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add("Cache-Control", "max-age=3600"); // Cache for 1 hour

        return ResponseEntity.ok().headers(headers).body(response);
    }

    @Operation(summary = "Get NAMASTE to ICD-11 ConceptMap",
            description = "Returns the FHIR ConceptMap resource for NAMASTE to ICD-11 mappings")
    @GetMapping("/ConceptMap/namaste-to-icd11")
    public ResponseEntity<String> getNamasteToIcd11ConceptMap(
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/ConceptMap/namaste-to-icd11", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        ConceptMap conceptMap = fhirService.generateNamasteToIcd11ConceptMap();

        String response;
        MediaType mediaType;

        if ("xml".equalsIgnoreCase(_format)) {
            response = fhirService.serializeToXml(conceptMap);
            mediaType = MediaType.APPLICATION_XML;
        } else {
            response = fhirService.serializeToJson(conceptMap);
            mediaType = MediaType.APPLICATION_JSON;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add("Cache-Control", "max-age=1800"); // Cache for 30 minutes

        return ResponseEntity.ok().headers(headers).body(response);
    }

    @Operation(summary = "Get NAMASTE ValueSet",
            description = "Returns a FHIR ValueSet resource for NAMASTE codes with optional filtering")
    @GetMapping("/ValueSet/namaste")
    public ResponseEntity<String> getNamasteValueSet(
            @Parameter(description = "Filter by display text") @RequestParam(required = false) String filter,
            @Parameter(description = "Filter by traditional system")
            @RequestParam(required = false) NamasteCode.TraditionalSystem system,
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/ValueSet/namaste", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        ValueSet valueSet = fhirService.generateNamasteValueSet(filter, system);

        String response;
        MediaType mediaType;

        if ("xml".equalsIgnoreCase(_format)) {
            response = fhirService.serializeToXml(valueSet);
            mediaType = MediaType.APPLICATION_XML;
        } else {
            response = fhirService.serializeToJson(valueSet);
            mediaType = MediaType.APPLICATION_JSON;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add("Cache-Control", "max-age=1800"); // Cache for 30 minutes

        return ResponseEntity.ok().headers(headers).body(response);
    }

    @Operation(summary = "Translate codes",
            description = "FHIR $translate operation for ConceptMap")
    @PostMapping("/ConceptMap/namaste-to-icd11/$translate")
    public ResponseEntity<String> translateConcept(
            @Parameter(description = "Source code to translate") @RequestParam String code,
            @Parameter(description = "Source system URI") @RequestParam String system,
            @Parameter(description = "Target system URI (optional)")
            @RequestParam(required = false) String targetsystem,
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/ConceptMap/namaste-to-icd11/$translate", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        Parameters parameters = new Parameters();
        parameters.setId("translate-result-" + System.currentTimeMillis());

        // Add result parameter
        Parameters.ParametersParameterComponent resultParam = parameters.addParameter();
        resultParam.setName("result");
        resultParam.setValue(new BooleanType(true)); // Simplified - would need actual translation logic

        // Add matches (simplified example)
        Parameters.ParametersParameterComponent matchParam = parameters.addParameter();
        matchParam.setName("match");

        Parameters.ParametersParameterComponent equivalenceParam = matchParam.addPart();
        equivalenceParam.setName("equivalence");
        equivalenceParam.setValue(new CodeType("equivalent"));

        Parameters.ParametersParameterComponent conceptParam = matchParam.addPart();
        conceptParam.setName("concept");
        Coding coding = new Coding();
        coding.setSystem("http://id.who.int/icd/release/11/2023-01/");
        coding.setCode("TM2-EXAMPLE-CODE");
        coding.setDisplay("Example TM2 Translation");
        conceptParam.setValue(coding);

        String response;
        MediaType mediaType;

        if ("xml".equalsIgnoreCase(_format)) {
            response = fhirService.serializeToXml(parameters);
            mediaType = MediaType.APPLICATION_XML;
        } else {
            response = fhirService.serializeToJson(parameters);
            mediaType = MediaType.APPLICATION_JSON;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);

        return ResponseEntity.ok().headers(headers).body(response);
    }

    @Operation(summary = "Upload FHIR Bundle",
            description = "Upload a FHIR Bundle containing Conditions with dual coding")
    @PostMapping("/Bundle")
    public ResponseEntity<String> uploadBundle(
            @Valid @RequestBody String bundleJson,
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/Bundle", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        try {
            // Parse the incoming bundle
            Bundle bundle = (Bundle) fhirContext.newJsonParser().parseResource(bundleJson);

            // Process the bundle (simplified - would need full processing logic)
            OperationOutcome outcome = new OperationOutcome();
            outcome.setId("bundle-processing-result");

            OperationOutcome.OperationOutcomeIssueComponent issue = outcome.addIssue();
            issue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
            issue.setCode(OperationOutcome.IssueType.INFORMATIONAL);
            issue.setDiagnostics("Bundle processed successfully with " +
                    bundle.getEntry().size() + " entries");

            String response;
            MediaType mediaType;

            if ("xml".equalsIgnoreCase(_format)) {
                response = fhirService.serializeToXml(outcome);
                mediaType = MediaType.APPLICATION_XML;
            } else {
                response = fhirService.serializeToJson(outcome);
                mediaType = MediaType.APPLICATION_JSON;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);

            return ResponseEntity.ok().headers(headers).body(response);

        } catch (Exception e) {
            OperationOutcome errorOutcome = new OperationOutcome();
            errorOutcome.setId("bundle-processing-error");

            OperationOutcome.OperationOutcomeIssueComponent errorIssue = errorOutcome.addIssue();
            errorIssue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            errorIssue.setCode(OperationOutcome.IssueType.PROCESSING);
            errorIssue.setDiagnostics("Failed to process bundle: " + e.getMessage());

            String errorResponse = fhirService.serializeToJson(errorOutcome);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @Operation(summary = "Create Condition with dual coding",
            description = "Create a FHIR Condition resource with NAMASTE and ICD-11 codes")
    @PostMapping("/Condition")
    public ResponseEntity<String> createCondition(
            @Valid @RequestBody CreateConditionRequest request,
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/Condition", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        try {
            Condition condition = fhirService.createConditionWithDualCoding(
                    request.getNamasteCode(), request.getPatientId());

            String response;
            MediaType mediaType;

            if ("xml".equalsIgnoreCase(_format)) {
                response = fhirService.serializeToXml(condition);
                mediaType = MediaType.APPLICATION_XML;
            } else {
                response = fhirService.serializeToJson(condition);
                mediaType = MediaType.APPLICATION_JSON;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.add("Location", "/fhir/Condition/" + condition.getId());

            return ResponseEntity.created(java.net.URI.create("/fhir/Condition/" + condition.getId()))
                    .headers(headers)
                    .body(response);

        } catch (Exception e) {
            OperationOutcome errorOutcome = new OperationOutcome();
            errorOutcome.setId("condition-creation-error");

            OperationOutcome.OperationOutcomeIssueComponent errorIssue = errorOutcome.addIssue();
            errorIssue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            errorIssue.setCode(OperationOutcome.IssueType.PROCESSING);
            errorIssue.setDiagnostics("Failed to create condition: " + e.getMessage());

            String errorResponse = fhirService.serializeToJson(errorOutcome);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    // Request DTOs
    public static class CreateConditionRequest {
        @jakarta.validation.constraints.NotBlank
        private String namasteCode;

        @jakarta.validation.constraints.NotBlank
        private String patientId;

        // Getters and Setters
        public String getNamasteCode() { return namasteCode; }
        public void setNamasteCode(String namasteCode) { this.namasteCode = namasteCode; }

        public String getPatientId() { return patientId; }
        public void setPatientId(String patientId) { this.patientId = patientId; }
    }
}