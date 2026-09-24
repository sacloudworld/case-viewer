package com.example.case_viewer.repository;

import com.example.case_viewer.entity.CaseEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CaseEmailRepository extends JpaRepository<CaseEmail, Long> {

    @Query("""
            SELECT e FROM CaseEmail e
            JOIN FETCH e.activity a
            WHERE a.caseEntity.id = :caseId
            ORDER BY a.activityAt ASC, a.id ASC
            """)
    List<CaseEmail> findByCaseId(@Param("caseId") Long caseId);
}
