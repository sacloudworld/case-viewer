package com.example.case_viewer.service;

import com.example.case_viewer.dto.ActivityRequest;
import com.example.case_viewer.dto.ActivityResponse;
import com.example.case_viewer.entity.Activity;
import com.example.case_viewer.entity.ActivityStatus;
import com.example.case_viewer.entity.Case;
import com.example.case_viewer.repository.ActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/**
 * Activities are only reachable through a case the current user owns.
 */
@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private final CaseService caseService;
    private final ActivityRepository activityRepository;
    private final DatabaseGuard databaseGuard;

    public ActivityService(
            CaseService caseService,
            ActivityRepository activityRepository,
            DatabaseGuard databaseGuard) {
        this.caseService = caseService;
        this.activityRepository = activityRepository;
        this.databaseGuard = databaseGuard;
    }

    @Transactional
    public ActivityResponse createActivity(Long caseId, ActivityRequest request, String owner) {

        log.info("Create activity caseId={}, type={}, owner={}",
                caseId, request.activityType(), owner);

        return databaseGuard.run(() -> {
            Case caseEntity = caseService.findOwnedCase(caseId, owner);

            Activity activity = new Activity();
            activity.setActivityType(request.activityType().trim());
            activity.setDescription(request.description().trim());
            activity.setStatus(request.status() != null ? request.status() : ActivityStatus.PENDING);
            activity.setPerformedBy(
                    request.performedBy() == null || request.performedBy().isBlank()
                            ? owner
                            : request.performedBy().trim());
            activity.setActivityAt(request.activityAt() != null ? request.activityAt() : Instant.now());

            caseEntity.addActivity(activity);

            return CaseService.toActivityResponse(activityRepository.saveAndFlush(activity));
        });
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> searchActivities(
            Long caseId, String query, ActivityStatus status, String owner) {

        log.info("Search activities caseId={}, status={}, query={}, owner={}",
                caseId, status, query, owner);

        return databaseGuard.run(() -> {
            Case caseEntity = caseService.findOwnedCase(caseId, owner);

            String pattern = query == null || query.isBlank()
                    ? null
                    : "%" + query.trim().toLowerCase(Locale.ROOT) + "%";

            return activityRepository.searchByCase(caseEntity.getId(), status, pattern)
                    .stream()
                    .map(CaseService::toActivityResponse)
                    .toList();
        });
    }
}
