package com.example.case_viewer.dto;

import com.example.case_viewer.entity.CaseStatus;

import java.time.Instant;

/**
 * A case as an inbox row. awaitingReply is true when the latest message came
 * from the customer and no agent has answered yet.
 */
public record ConversationSummary(
        Long caseId,
        String subject,
        CaseStatus status,
        String customer,
        Instant createdAt,
        Instant lastMessageAt,
        String lastDirection,
        String lastPreview,
        boolean awaitingReply
) {
}
