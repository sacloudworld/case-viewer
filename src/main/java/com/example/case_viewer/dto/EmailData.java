package com.example.case_viewer.dto;

import java.time.Instant;

/** The JSON document stored in case_emails.email_data. */
public record EmailData(
        String from,
        String to,
        String subject,
        String body,
        Instant sentAt
) {
}
