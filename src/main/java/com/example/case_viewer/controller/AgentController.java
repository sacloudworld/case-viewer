package com.example.case_viewer.controller;

import com.example.case_viewer.dto.ConversationDetail;
import com.example.case_viewer.dto.ConversationSummary;
import com.example.case_viewer.dto.StatusUpdateRequest;
import com.example.case_viewer.entity.CaseStatus;
import com.example.case_viewer.service.SecureInboxService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * Agent console (role AGENT): sees every customer's conversations and replies to them.
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final SecureInboxService inboxService;

    public AgentController(SecureInboxService inboxService) {
        this.inboxService = inboxService;
    }

    // GET /api/agent/conversations?q=alice&status=OPEN
    @GetMapping("/conversations")
    public List<ConversationSummary> inbox(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(required = false) CaseStatus status) {
        return inboxService.agentInbox(query, status);
    }

    @GetMapping("/conversations/{caseId}")
    public ConversationDetail conversation(@PathVariable Long caseId) {
        return inboxService.agentConversation(caseId);
    }

    // AGENT REPLY -> OUTBOUND ACTIVITY (multipart: body, files)
    @PostMapping(value = "/conversations/{caseId}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationDetail reply(
            @PathVariable Long caseId,
            @RequestParam String body,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Principal principal) {
        return inboxService.agentReply(caseId, principal.getName(), body, files);
    }

    @PatchMapping("/conversations/{caseId}/status")
    public ConversationDetail updateStatus(
            @PathVariable Long caseId,
            @Valid @RequestBody StatusUpdateRequest request,
            Principal principal) {
        return inboxService.updateStatus(caseId, request.status(), principal.getName());
    }

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<byte[]> attachment(@PathVariable Long attachmentId) {
        return AttachmentDownloads.toResponse(inboxService.agentAttachment(attachmentId));
    }
}
