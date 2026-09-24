package com.example.case_viewer.repository;

import com.example.case_viewer.entity.Case;
import com.example.case_viewer.entity.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<Case, Long> {

    // Owner-scoped lookup: a case is only visible to the customer who created it
    Optional<Case> findByIdAndOwnerUsername(Long caseId, String ownerUsername);

    // Owner-scoped search; pattern is a lower-cased "%text%" or null to match all
    @Query("""
            SELECT c FROM Case c
            WHERE c.ownerUsername = :owner
              AND (:status IS NULL OR c.status = :status)
              AND (:pattern IS NULL
                   OR CAST(c.id AS String) LIKE :pattern
                   OR LOWER(c.title) LIKE :pattern
                   OR LOWER(c.description) LIKE :pattern)
            ORDER BY c.createdAt DESC
            """)
    List<Case> searchOwnedCases(
            @Param("owner") String owner,
            @Param("status") CaseStatus status,
            @Param("pattern") String pattern);

    // Agent view: every customer's cases; pattern also matches the customer's username
    @Query("""
            SELECT c FROM Case c
            WHERE (:status IS NULL OR c.status = :status)
              AND (:pattern IS NULL
                   OR CAST(c.id AS String) LIKE :pattern
                   OR LOWER(c.title) LIKE :pattern
                   OR LOWER(c.ownerUsername) LIKE :pattern)
            ORDER BY c.createdAt DESC
            """)
    List<Case> searchAllCases(
            @Param("status") CaseStatus status,
            @Param("pattern") String pattern);


    

    /* 

    // 2. Find by status
    List<Case> findByStatus(String status);

    // 3. Find by priority
    List<Case> findByPriority(String priority);

    // 4. AND condition
    List<Case> findByStatusAndPriority(
            String status,
            String priority
    );

    // 5. OR condition
    List<Case> findByStatusOrPriority(
            String status,
            String priority
    );

    // 6. Contains
    List<Case> findByCustomerNameContaining(
            String customerName
    );

    // 7. Starts with
    List<Case> findByCaseNumberStartingWith(
            String prefix
    );

    // 8. Case-insensitive
    Optional<Case> findByCaseNumberIgnoreCase(
            String caseNumber
    );

    // 9. IN condition
    List<Case> findByStatusIn(
            List<String> statuses
    );

    // 10. NOT condition
    List<Case> findByStatusNot(
            String status
    );

    // 11. Greater than
    List<Case> findByCreatedAtAfter(
            LocalDateTime date
    );

    // 12. Before
    List<Case> findByCreatedAtBefore(
            LocalDateTime date
    );

    // 13. Between
    List<Case> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // 14. Filter + sorting
    List<Case> findByStatusOrderByCreatedAtDesc(
            String status
    );

    // 15. Multiple conditions + sorting
    List<Case> findByStatusAndPriorityOrderByCreatedAtDesc(
            String status,
            String priority
    );

    // 16. Count
    long countByStatus(String status);

    // 17. Exists
    // 18. Delete
    void deleteByCaseNumber(String caseNumber);

    */
}