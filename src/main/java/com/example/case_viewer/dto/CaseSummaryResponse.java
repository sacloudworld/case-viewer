package com.example.case_viewer.dto;

import com.example.case_viewer.entity.CaseStatus;

import java.time.Instant;

/** A case row in search results, without its activities. */
public record CaseSummaryResponse(
        Long caseId,
        String title,
        CaseStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
