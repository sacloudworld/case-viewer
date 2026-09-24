package com.example.case_viewer.controller;

import com.example.case_viewer.entity.EmailAttachment;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

/** Builds the download response for an attachment; always served as a download, never inline. */
final class AttachmentDownloads {

    private AttachmentDownloads() {
    }

    static ResponseEntity<byte[]> toResponse(EmailAttachment attachment) {
        MediaType type;
        try {
            type = attachment.getContentType() == null
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(attachment.getContentType());
        } catch (IllegalArgumentException e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(attachment.getFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(attachment.getAttachmentData());
    }
}
