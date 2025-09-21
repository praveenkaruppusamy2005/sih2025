package in.gov.ayush.terminology.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "namaste_codes")
public class NamasteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank
    private String code;

    @Column(nullable = false)
    @NotBlank
    private String display;

    private String definition;

    @Enumerated(EnumType.STRING)
    private TraditionalSystem system; // AYURVEDA, SIDDHA, UNANI

    private String category;
    private String subcategory;

    @Column(name = "who_terminology_code")
    private String whoTerminologyCode;

    @Column(name = "icd11_tm2_code")
    private String icd11Tm2Code;

    @Column(name = "icd11_biomedicine_code")
    private String icd11BiomedicineCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "version")
    private String version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public NamasteCode() {}

    public NamasteCode(String code, String display, TraditionalSystem system) {
        this.code = code;
        this.display = display;
        this.system = system;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public TraditionalSystem getSystem() { return system; }
    public void setSystem(TraditionalSystem system) { this.system = system; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getWhoTerminologyCode() { return whoTerminologyCode; }
    public void setWhoTerminologyCode(String whoTerminologyCode) {
        this.whoTerminologyCode = whoTerminologyCode;
    }

    public String getIcd11Tm2Code() { return icd11Tm2Code; }
    public void setIcd11Tm2Code(String icd11Tm2Code) { this.icd11Tm2Code = icd11Tm2Code; }

    public String getIcd11BiomedicineCode() { return icd11BiomedicineCode; }
    public void setIcd11BiomedicineCode(String icd11BiomedicineCode) {
        this.icd11BiomedicineCode = icd11BiomedicineCode;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public enum TraditionalSystem {
        AYURVEDA, SIDDHA, UNANI
    }
}