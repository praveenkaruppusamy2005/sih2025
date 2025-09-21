package in.gov.ayush.terminology.repository;

import in.gov.ayush.terminology.model.Icd11Code;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Icd11Repository extends JpaRepository<Icd11Code, Long> {

    Optional<Icd11Code> findByCode(String code);

    List<Icd11Code> findByCodeType(Icd11Code.CodeType codeType);

    List<Icd11Code> findByChapter(String chapter);

    @Query("SELECT i FROM Icd11Code i WHERE " +
            "LOWER(i.title) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(i.code) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(i.definition) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<Icd11Code> searchByTerm(@Param("term") String term, Pageable pageable);

    @Query("SELECT i FROM Icd11Code i WHERE " +
            "LOWER(i.title) LIKE LOWER(CONCAT(:term, '%')) OR " +
            "LOWER(i.code) LIKE LOWER(CONCAT(:term, '%'))")
    List<Icd11Code> findForAutoComplete(@Param("term") String term, Pageable pageable);

    List<Icd11Code> findByParent(String parent);

    @Query("SELECT DISTINCT i.chapter FROM Icd11Code i WHERE i.codeType = :codeType")
    List<String> findChaptersByCodeType(@Param("codeType") Icd11Code.CodeType codeType);
}