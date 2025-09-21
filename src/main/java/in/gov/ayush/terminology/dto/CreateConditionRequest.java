package in.gov.ayush.terminology.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateConditionRequest {

    @NotBlank(message = "NAMASTE code is required")
    private String namasteCode;

    @NotBlank(message = "Patient ID is required")
    private String patientId;

    @Pattern(regexp = "active|inactive|resolved", message = "Clinical status must be active, inactive, or resolved")
    private String clinicalStatus;

    @Pattern(regexp = "provisional|differential|confirmed|refuted|entered-in-error|unknown", 
             message = "Verification status must be one of: provisional, differential, confirmed, refuted, entered-in-error, unknown")
    private String verificationStatus;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Onset date must be in YYYY-MM-DD format")
    private String onsetDate;

    private String notes;

    // Constructors
    public CreateConditionRequest() {}

    public CreateConditionRequest(String namasteCode, String patientId) {
        this.namasteCode = namasteCode;
        this.patientId = patientId;
    }

    // Getters and Setters
    public String getNamasteCode() {
        return namasteCode;
    }

    public void setNamasteCode(String namasteCode) {
        this.namasteCode = namasteCode;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getClinicalStatus() {
        return clinicalStatus;
    }

    public void setClinicalStatus(String clinicalStatus) {
        this.clinicalStatus = clinicalStatus;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getOnsetDate() {
        return onsetDate;
    }

    public void setOnsetDate(String onsetDate) {
        this.onsetDate = onsetDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CreateConditionRequest{" +
                "namasteCode='" + namasteCode + '\'' +
                ", patientId='" + patientId + '\'' +
                ", clinicalStatus='" + clinicalStatus + '\'' +
                ", verificationStatus='" + verificationStatus + '\'' +
                ", onsetDate='" + onsetDate + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}