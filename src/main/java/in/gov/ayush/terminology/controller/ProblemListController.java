package in.gov.ayush.terminology.controller;

import ca.uhn.fhir.context.FhirContext;
import in.gov.ayush.terminology.dto.CreateConditionRequest;
import in.gov.ayush.terminology.service.FhirService;
import in.gov.ayush.terminology.service.ProblemListService;
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
import java.util.Map;

@RestController
@RequestMapping("/fhir/ProblemList")
@Tag(name = "Problem List API", description = "FHIR Problem List with dual coding support")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProblemListController {

    @Autowired
    private ProblemListService problemListService;

    @Autowired
    private FhirService fhirService;

    @Autowired
    private FhirContext fhirContext;

    @Autowired
    private AuditLogger auditLogger;

    @Operation(summary = "Create dual-coded Condition",
            description = "Create a FHIR Condition with both NAMASTE and ICD-11 codes")
    @PostMapping("/Condition")
    public ResponseEntity<String> createDualCodedCondition(
            @Valid @RequestBody CreateConditionRequest request,
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/ProblemList/Condition", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        try {
            Condition condition = problemListService.createDualCodedCondition(
                    request.getNamasteCode(),
                    request.getPatientId(),
                    request.getClinicalStatus(),
                    request.getVerificationStatus(),
                    request.getOnsetDate(),
                    request.getNotes()
            );

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
            headers.add("Location", "/fhir/ProblemList/Condition/" + condition.getId());

            return ResponseEntity.created(java.net.URI.create("/fhir/ProblemList/Condition/" + condition.getId()))
                    .headers(headers)
                    .body(response);

        } catch (Exception e) {
            OperationOutcome errorOutcome = new OperationOutcome();
            errorOutcome.setId("condition-creation-error");

            OperationOutcome.OperationOutcomeIssueComponent errorIssue = errorOutcome.addIssue();
            errorIssue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            errorIssue.setCode(OperationOutcome.IssueType.PROCESSING);
            errorIssue.setDiagnostics("Failed to create dual-coded condition: " + e.getMessage());

            String errorResponse = fhirService.serializeToJson(errorOutcome);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @Operation(summary = "Process dual-coded Bundle",
            description = "Process a FHIR Bundle containing multiple dual-coded Conditions")
    @PostMapping("/Bundle")
    public ResponseEntity<String> processDualCodedBundle(
            @RequestBody String bundleJson,
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/ProblemList/Bundle", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        try {
            Bundle bundle = (Bundle) fhirContext.newJsonParser().parseResource(bundleJson);
            Bundle processedBundle = problemListService.processDualCodedBundle(bundle);

            String response;
            MediaType mediaType;

            if ("xml".equalsIgnoreCase(_format)) {
                response = fhirService.serializeToXml(processedBundle);
                mediaType = MediaType.APPLICATION_XML;
            } else {
                response = fhirService.serializeToJson(processedBundle);
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
            errorIssue.setDiagnostics("Failed to process dual-coded bundle: " + e.getMessage());

            String errorResponse = fhirService.serializeToJson(errorOutcome);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @Operation(summary = "Get dual-coding autocomplete suggestions",
            description = "Get autocomplete suggestions for dual coding")
    @GetMapping("/ValueSet/dual-coding-autocomplete")
    public ResponseEntity<String> getDualCodingAutoComplete(
            @Parameter(description = "Search term") @RequestParam String term,
            @Parameter(description = "Result limit") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/ProblemList/ValueSet/dual-coding-autocomplete", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        try {
            ValueSet valueSet = problemListService.generateDualCodingAutoCompleteValueSet(term, limit);

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

            return ResponseEntity.ok().headers(headers).body(response);

        } catch (Exception e) {
            OperationOutcome errorOutcome = new OperationOutcome();
            errorOutcome.setId("autocomplete-error");

            OperationOutcome.OperationOutcomeIssueComponent errorIssue = errorOutcome.addIssue();
            errorIssue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            errorIssue.setCode(OperationOutcome.IssueType.PROCESSING);
            errorIssue.setDiagnostics("Failed to generate autocomplete suggestions: " + e.getMessage());

            String errorResponse = fhirService.serializeToJson(errorOutcome);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @Operation(summary = "Get patient problem list",
            description = "Get all conditions for a specific patient")
    @GetMapping("/Condition")
    public ResponseEntity<String> getPatientProblemList(
            @Parameter(description = "Patient ID") @RequestParam String patient,
            @Parameter(description = "Response format")
            @RequestParam(defaultValue = "json") String _format,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/ProblemList/Condition", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        try {
            Bundle problemListBundle = problemListService.getPatientProblemList(patient);

            String response;
            MediaType mediaType;

            if ("xml".equalsIgnoreCase(_format)) {
                response = fhirService.serializeToXml(problemListBundle);
                mediaType = MediaType.APPLICATION_XML;
            } else {
                response = fhirService.serializeToJson(problemListBundle);
                mediaType = MediaType.APPLICATION_JSON;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);

            return ResponseEntity.ok().headers(headers).body(response);

        } catch (Exception e) {
            OperationOutcome errorOutcome = new OperationOutcome();
            errorOutcome.setId("problem-list-error");

            OperationOutcome.OperationOutcomeIssueComponent errorIssue = errorOutcome.addIssue();
            errorIssue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            errorIssue.setCode(OperationOutcome.IssueType.PROCESSING);
            errorIssue.setDiagnostics("Failed to retrieve problem list: " + e.getMessage());

            String errorResponse = fhirService.serializeToJson(errorOutcome);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    @Operation(summary = "Get coding suggestions",
            description = "Get coding suggestions for a given term")
    @GetMapping("/coding-suggestions")
    public ResponseEntity<Map<String, Object>> getCodingSuggestions(
            @Parameter(description = "Search term") @RequestParam String term,
            @Parameter(description = "Result limit") @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/ProblemList/coding-suggestions", "GET",
                authentication != null ? authentication.getName() : "anonymous");

        try {
            Map<String, Object> suggestions = problemListService.getCodingSuggestions(term, limit);
            return ResponseEntity.ok(suggestions);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to get coding suggestions: " + e.getMessage()
            ));
        }
    }

    @Operation(summary = "Validate dual coding",
            description = "Validate a dual coding combination")
    @PostMapping("/validate-coding")
    public ResponseEntity<Map<String, Object>> validateDualCoding(
            @RequestBody Map<String, String> codingRequest,
            Authentication authentication) {

        auditLogger.logApiAccess("/fhir/ProblemList/validate-coding", "POST",
                authentication != null ? authentication.getName() : "anonymous");

        try {
            String namasteCode = codingRequest.get("namasteCode");
            String icd11Code = codingRequest.get("icd11Code");
            String system = codingRequest.get("system");

            Map<String, Object> validation = problemListService.validateDualCoding(
                    namasteCode, icd11Code, system);

            return ResponseEntity.ok(validation);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to validate dual coding: " + e.getMessage()
            ));
        }
    }
}