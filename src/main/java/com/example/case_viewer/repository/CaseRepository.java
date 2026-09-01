package com.example.case_viewer.repository;

import com.example.case_viewer.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CaseRepository extends JpaRepository<Case, UUID> {

    // 1. Exact match
    Optional<Case> findByCaseNumber(String caseNumber);

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
    boolean existsByCaseNumber(String caseNumber);

    // 18. Delete
    void deleteByCaseNumber(String caseNumber);

    */
}