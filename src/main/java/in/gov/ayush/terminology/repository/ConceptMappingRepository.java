package in.gov.ayush.terminology.repository;

import in.gov.ayush.terminology.model.ConceptMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptMappingRepository extends JpaRepository<ConceptMapping, Long> {

    List<ConceptMapping> findBySourceCodeAndSourceSystem(String sourceCode, String sourceSystem);

    List<ConceptMapping> findByTargetCodeAndTargetSystem(String targetCode, String targetSystem);

    List<ConceptMapping> findBySourceSystem(String sourceSystem);

    List<ConceptMapping> findByTargetSystem(String targetSystem);

    List<ConceptMapping> findByEquivalence(ConceptMapping.MappingEquivalence equivalence);

    @Query("SELECT m FROM ConceptMapping m WHERE " +
            "(m.sourceCode = :code AND m.sourceSystem = :system) OR " +
            "(m.targetCode = :code AND m.targetSystem = :system)")
    List<ConceptMapping> findMappingsForCode(@Param("code") String code,
                                             @Param("system") String system);

    @Query("SELECT m FROM ConceptMapping m WHERE " +
            "m.sourceSystem = :sourceSystem AND m.targetSystem = :targetSystem")
    List<ConceptMapping> findBySourceAndTargetSystem(@Param("sourceSystem") String sourceSystem,
                                                     @Param("targetSystem") String targetSystem);

    Optional<ConceptMapping> findBySourceCodeAndSourceSystemAndTargetSystem(
            String sourceCode, String sourceSystem, String targetSystem);
}