package com.example.case_viewer.dto;

import com.example.case_viewer.entity.ActivityStatus;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
        UUID activityId,
        String activityType,
        String description,
        ActivityStatus status,
        String performedBy,
        Instant activityAt
) {
}
