package com.example.case_viewer.dto;

import com.example.case_viewer.entity.CaseStatus;

import java.time.Instant;
import java.util.List;

public record CaseResponse(
        Long caseId,
        String title,
        String description,
        CaseStatus status,
        List<ActivityResponse> activities,
        Instant createdAt,
        Instant updatedAt
) {
}
