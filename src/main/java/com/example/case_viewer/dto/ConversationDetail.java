package com.example.case_viewer.dto;

import com.example.case_viewer.entity.CaseStatus;

import java.time.Instant;
import java.util.List;

public record ConversationDetail(
        Long caseId,
        String subject,
        CaseStatus status,
        String customer,
        Instant createdAt,
        Instant updatedAt,
        List<MessageResponse> messages
) {
}
