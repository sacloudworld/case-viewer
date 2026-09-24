package com.example.case_viewer.controller;

import com.example.case_viewer.dto.ConversationDetail;
import com.example.case_viewer.dto.ConversationSummary;
import com.example.case_viewer.entity.CaseStatus;
import com.example.case_viewer.service.SecureInboxService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * Customer secure inbox (role USER). Every call is scoped to the signed-in customer's own cases.
 */
@RestController
@RequestMapping("/api/inbox")
public class InboxController {

    private final SecureInboxService inboxService;

    public InboxController(SecureInboxService inboxService) {
        this.inboxService = inboxService;
    }

    // GET /api/inbox/conversations?q=invoice&status=OPEN
    @GetMapping("/conversations")
    public List<ConversationSummary> inbox(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(required = false) CaseStatus status,
            Principal principal) {
        return inboxService.customerInbox(principal.getName(), query, status);
    }

    @GetMapping("/conversations/{caseId}")
    public ConversationDetail conversation(@PathVariable Long caseId, Principal principal) {
        return inboxService.customerConversation(caseId, principal.getName());
    }

    // NEW EMAIL -> NEW CASE (multipart: subject, body, files)
    @PostMapping(value = "/conversations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationDetail send(
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Principal principal) {
        return inboxService.startConversation(principal.getName(), subject, body, files);
    }

    // REPLY -> SAME CASE (multipart: body, files)
    @PostMapping(value = "/conversations/{caseId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationDetail reply(
            @PathVariable Long caseId,
            @RequestParam String body,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Principal principal) {
        return inboxService.customerReply(caseId, principal.getName(), body, files);
    }

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<byte[]> attachment(@PathVariable Long attachmentId, Principal principal) {
        return AttachmentDownloads.toResponse(inboxService.customerAttachment(attachmentId, principal.getName()));
    }
}
