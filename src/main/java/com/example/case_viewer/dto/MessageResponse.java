package com.example.case_viewer.dto;

import java.time.Instant;
import java.util.List;

/** One message in a conversation; direction is INBOUND (customer) or OUTBOUND (agent). */
public record MessageResponse(
        Long emailId,
        Long activityId,
        String direction,
        String from,
        String to,
        String subject,
        String body,
        Instant sentAt,
        List<AttachmentInfo> attachments
) {
}
