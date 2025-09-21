package in.gov.ayush.terminology.repository;


import in.gov.ayush.terminology.model.NamasteCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NamasteRepository extends JpaRepository<NamasteCode, Long> {

    Optional<NamasteCode> findByCode(String code);

    List<NamasteCode> findBySystem(NamasteCode.TraditionalSystem system);

    @Query("SELECT n FROM NamasteCode n WHERE " +
            "LOWER(n.display) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(n.code) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(n.definition) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<NamasteCode> searchByTerm(@Param("term") String term, Pageable pageable);

    @Query("SELECT n FROM NamasteCode n WHERE " +
            "LOWER(n.display) LIKE LOWER(CONCAT(:term, '%')) OR " +
            "LOWER(n.code) LIKE LOWER(CONCAT(:term, '%'))")
    List<NamasteCode> findForAutoComplete(@Param("term") String term, Pageable pageable);

    List<NamasteCode> findByCategory(String category);

    List<NamasteCode> findBySystemAndCategory(NamasteCode.TraditionalSystem system, String category);

    @Query("SELECT DISTINCT n.category FROM NamasteCode n WHERE n.system = :system")
    List<String> findCategoriesBySystem(@Param("system") NamasteCode.TraditionalSystem system);

    List<NamasteCode> findByWhoTerminologyCodeIsNotNull();

    List<NamasteCode> findByIcd11Tm2CodeIsNotNull();

    List<NamasteCode> findByIcd11BiomedicineCodeIsNotNull();
}