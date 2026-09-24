package com.example.case_viewer.dto;

import com.example.case_viewer.entity.CaseStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required")
        CaseStatus status
) {
}
