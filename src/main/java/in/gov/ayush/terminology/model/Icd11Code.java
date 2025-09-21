package in.gov.ayush.terminology.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "icd11_codes")
public class Icd11Code {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String definition;

    @Enumerated(EnumType.STRING)
    private CodeType codeType; 

    private String parent;
    private String chapter;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "icd11_synonyms")
    @Column(name = "synonym")
    private Map<String, String> synonyms;

    @Column(name = "linearization_uri")
    private String linearizationUri;

    @Column(name = "foundation_uri")
    private String foundationUri;

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
    public Icd11Code() {}

    public Icd11Code(String code, String title, CodeType codeType) {
        this.code = code;
        this.title = title;
        this.codeType = codeType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public CodeType getCodeType() { return codeType; }
    public void setCodeType(CodeType codeType) { this.codeType = codeType; }

    public String getParent() { return parent; }
    public void setParent(String parent) { this.parent = parent; }

    public String getChapter() { return chapter; }
    public void setChapter(String chapter) { this.chapter = chapter; }

    public Map<String, String> getSynonyms() { return synonyms; }
    public void setSynonyms(Map<String, String> synonyms) { this.synonyms = synonyms; }

    public String getLinearizationUri() { return linearizationUri; }
    public void setLinearizationUri(String linearizationUri) {
        this.linearizationUri = linearizationUri;
    }

    public String getFoundationUri() { return foundationUri; }
    public void setFoundationUri(String foundationUri) { this.foundationUri = foundationUri; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum CodeType {
        TM2, BIOMEDICINE
    }
}