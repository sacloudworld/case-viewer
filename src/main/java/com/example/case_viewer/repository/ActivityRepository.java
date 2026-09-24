package com.example.case_viewer.repository;

import com.example.case_viewer.entity.Activity;
import com.example.case_viewer.entity.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // pattern is a lower-cased "%text%" or null to match all
    @Query("""
            SELECT a FROM Activity a
            WHERE a.caseEntity.id = :caseId
              AND (:status IS NULL OR a.status = :status)
              AND (:pattern IS NULL
                   OR LOWER(a.activityType) LIKE :pattern
                   OR LOWER(a.description) LIKE :pattern
                   OR LOWER(a.performedBy) LIKE :pattern)
            ORDER BY a.activityAt DESC
            """)
    List<Activity> searchByCase(
            @Param("caseId") Long caseId,
            @Param("status") ActivityStatus status,
            @Param("pattern") String pattern);

    // The most recent activity of each given case (drives inbox ordering and "awaiting reply")
    @Query("""
            SELECT a FROM Activity a
            WHERE a.caseEntity.id IN :caseIds
              AND a.id = (SELECT MAX(a2.id) FROM Activity a2 WHERE a2.caseEntity = a.caseEntity)
            """)
    List<Activity> findLatestByCaseIds(@Param("caseIds") List<Long> caseIds);

    @Modifying
    @Query("""
            UPDATE Activity a SET a.status = :to
            WHERE a.caseEntity.id = :caseId
              AND a.activityType = :type
              AND a.status = :from
            """)
    int updateStatusByType(
            @Param("caseId") Long caseId,
            @Param("type") String type,
            @Param("from") ActivityStatus from,
            @Param("to") ActivityStatus to);
}
