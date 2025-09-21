package in.gov.ayush.terminology.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "concept_mappings")
public class ConceptMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_code", nullable = false)
    private String sourceCode;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "target_code", nullable = false)
    private String targetCode;

    @Column(name = "target_system", nullable = false)
    private String targetSystem;

    @Enumerated(EnumType.STRING)
    private MappingEquivalence equivalence;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "mapping_version")
    private String mappingVersion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
    public ConceptMapping() {}

    public ConceptMapping(String sourceCode, String sourceSystem,
                          String targetCode, String targetSystem,
                          MappingEquivalence equivalence) {
        this.sourceCode = sourceCode;
        this.sourceSystem = sourceSystem;
        this.targetCode = targetCode;
        this.targetSystem = targetSystem;
        this.equivalence = equivalence;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }

    public String getTargetCode() { return targetCode; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }

    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }

    public MappingEquivalence getEquivalence() { return equivalence; }
    public void setEquivalence(MappingEquivalence equivalence) { this.equivalence = equivalence; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getMappingVersion() { return mappingVersion; }
    public void setMappingVersion(String mappingVersion) { this.mappingVersion = mappingVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum MappingEquivalence {
        RELATEDTO, EQUIVALENT, EQUAL, WIDER, SUBSUMES, NARROWER, SPECIALIZES,
        INEXACT, UNMATCHED, DISJOINT
    }
}