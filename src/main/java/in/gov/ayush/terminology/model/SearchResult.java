package in.gov.ayush.terminology.model;

import java.util.List;

public class SearchResult {
    private String code;
    private String display;
    private String system;
    private String definition;
    private List<ConceptMapping> mappings;
    private Double score;

    // Constructors
    public SearchResult() {}

    public SearchResult(String code, String display, String system) {
        this.code = code;
        this.display = display;
        this.system = system;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }

    public String getSystem() { return system; }
    public void setSystem(String system) { this.system = system; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public List<ConceptMapping> getMappings() { return mappings; }
    public void setMappings(List<ConceptMapping> mappings) { this.mappings = mappings; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}