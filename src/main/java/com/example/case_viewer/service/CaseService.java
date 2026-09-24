package com.example.case_viewer.service;

import com.example.case_viewer.dto.ActivityResponse;
import com.example.case_viewer.dto.CaseResponse;
import com.example.case_viewer.dto.CaseSummaryResponse;
import com.example.case_viewer.entity.Activity;
import com.example.case_viewer.entity.Case;
import com.example.case_viewer.entity.CaseStatus;
import com.example.case_viewer.exception.CaseNotFoundException;
import com.example.case_viewer.repository.CaseRepository;
import com.example.case_viewer.repository.CaseJDBCRepository;

import com.example.case_viewer.dto.CaseRequest;
import com.example.case_viewer.mapper.CaseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.case_viewer.aspect.AuditLog;

import java.util.List;
import java.util.Locale;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    private final CaseRepository caseRepository;
    private final DatabaseGuard databaseGuard;
    private final CaseMapper caseMapper;

    private final CaseJDBCRepository caseJDBCRepository;



    public CaseService(CaseRepository caseRepository, DatabaseGuard databaseGuard, CaseMapper caseMapper,
        CaseJDBCRepository caseJDBCRepository) {
        this.caseRepository = caseRepository;
        this.databaseGuard = databaseGuard;
        this.caseMapper = caseMapper;
        this.caseJDBCRepository = caseJDBCRepository;
    }

    @Transactional(readOnly = true)
    public CaseResponse getCase(Long caseId, String owner) {

        log.info("Fetching case with caseId={}, owner={}", caseId, owner);

        return databaseGuard.run(() -> {
            Case caseEntity = findOwnedCase(caseId, owner);

            List<ActivityResponse> activities = caseEntity.getActivities()
                    .stream()
                    .map(CaseService::toActivityResponse)
                    .toList();

            return toCaseResponse(caseEntity, activities);
        });
    }

    @Transactional(readOnly = true)
    public List<CaseSummaryResponse> searchCases(String query, CaseStatus status, String owner) {

        log.info("Searching cases owner={}, status={}, query={}", owner, status, query);

        return databaseGuard.run(() -> caseRepository
                .searchOwnedCases(owner, status, toLikePattern(query))
                .stream()
                .map(c -> new CaseSummaryResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getStatus(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()))
                .toList());
    }


    @Transactional
    public CaseResponse createCase(CaseRequest request, String owner) {

        log.info("Create case owner={}", owner);

        return databaseGuard.run(() -> {

            Case caseInfo = new Case();

            caseInfo.setTitle(request.title().trim());
            caseInfo.setDescription(request.description());
            caseInfo.setStatus(request.status() != null ? request.status() : CaseStatus.OPEN);
            caseInfo.setOwnerUsername(owner);

            Case caseEntity = caseRepository.saveAndFlush(caseInfo);

            return toCaseResponse(caseEntity, List.of());
        });
    }

    /**
     * Loads a case the given user owns, or throws {@link CaseNotFoundException}.
     * Cases owned by someone else are reported as not found so their existence is not disclosed.
     */
    Case findOwnedCase(Long caseId, String owner) {
        return caseRepository.findByIdAndOwnerUsername(caseId, owner)
                .orElseThrow(() -> new CaseNotFoundException("Case not found: " + caseId));
    }

    @AuditLog
    public CaseResponse findCase(
        Long caseId, String owner) {

            com.example.case_viewer.mybatis.entity.Case caseEntity = caseMapper.findCase(
                caseId, owner);


            if (caseEntity == null) {
                return null;
            }

           // CaseResponse response = new CaseResponse();

            return new CaseResponse(
                caseEntity.getCaseId(),
                caseEntity.getTitle(),
                caseEntity.getDescription(),
                caseEntity.getStatus(),
                List.of(),
                caseEntity.getCreatedAt(),
                caseEntity.getUpdatedAt()
        );

}


    public List<com.example.case_viewer.mapper.CaseResponse> getAllCases() {
        return caseJDBCRepository.getAllCases();
    }


    public com.example.case_viewer.mapper.CaseResponse getJDBCCase(Long caseId) {
        return caseJDBCRepository.getJDBCCase(caseId);
    }

    private static String toLikePattern(String query) {
        return isBlank(query) ? null : "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static CaseResponse toCaseResponse(Case caseEntity, List<ActivityResponse> activities) {
        return new CaseResponse(
                caseEntity.getId(),
                caseEntity.getTitle(),
                caseEntity.getDescription(),
                caseEntity.getStatus(),
                activities,
                caseEntity.getCreatedAt(),
                caseEntity.getUpdatedAt()
        );
    }

    static ActivityResponse toActivityResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityType(),
                activity.getDescription(),
                activity.getStatus(),
                activity.getPerformedBy(),
                activity.getActivityAt()
        );
    }
}
