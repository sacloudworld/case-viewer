package com.example.case_viewer.dto;

import com.example.case_viewer.entity.ActivityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * status defaults to PENDING, performedBy to the current user and
 * activityAt to now when omitted.
 */
public record ActivityRequest(
        @NotBlank(message = "Activity type is required")
        @Size(max = 100, message = "Activity type must be at most 100 characters")
        String activityType,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        ActivityStatus status,

        @Size(max = 100, message = "Performed by must be at most 100 characters")
        String performedBy,

        Instant activityAt
) {
}
