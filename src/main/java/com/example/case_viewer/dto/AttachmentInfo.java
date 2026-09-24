package com.example.case_viewer.dto;

/** Attachment metadata (no file bytes). */
public record AttachmentInfo(
        Long attachmentId,
        Long emailId,
        Long activityId,
        String fileName,
        String contentType,
        long sizeBytes
) {
}
