package com.example.case_viewer.service;

import com.example.case_viewer.dto.AttachmentInfo;
import com.example.case_viewer.dto.ConversationDetail;
import com.example.case_viewer.dto.ConversationSummary;
import com.example.case_viewer.dto.EmailData;
import com.example.case_viewer.dto.MessageResponse;
import com.example.case_viewer.entity.Activity;
import com.example.case_viewer.entity.ActivityStatus;
import com.example.case_viewer.entity.ActivityType;
import com.example.case_viewer.entity.Case;
import com.example.case_viewer.entity.CaseEmail;
import com.example.case_viewer.entity.CaseStatus;
import com.example.case_viewer.entity.EmailAttachment;
import com.example.case_viewer.exception.AttachmentNotFoundException;
import com.example.case_viewer.exception.CaseNotFoundException;
import com.example.case_viewer.repository.ActivityRepository;
import com.example.case_viewer.repository.CaseEmailRepository;
import com.example.case_viewer.repository.CaseRepository;
import com.example.case_viewer.repository.EmailAttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Secure inbox: customers message support and agents reply.
 *
 * <ul>
 * <li>A new customer message opens a new case with an INBOUND activity.</li>
 * <li>A customer reply on an existing case adds an INBOUND activity to that case.</li>
 * <li>An agent reply adds an OUTBOUND activity and marks pending INBOUND activities completed.</li>
 * </ul>
 * Each message is stored in case_emails (JSON email_data) and its files in email_attachments.
 * Customers only ever reach their own cases; agents reach all cases.
 */
@Service
public class SecureInboxService {

    private static final Logger log = LoggerFactory.getLogger(SecureInboxService.class);

    static final String SUPPORT_ADDRESS = "Support";
    private static final int MAX_ATTACHMENTS = 5;
    private static final int MAX_BODY_LENGTH = 20000;
    private static final int PREVIEW_LENGTH = 140;

    private final CaseRepository caseRepository;
    private final ActivityRepository activityRepository;
    private final CaseEmailRepository emailRepository;
    private final EmailAttachmentRepository attachmentRepository;
    private final DatabaseGuard databaseGuard;
    private final JsonMapper jsonMapper;

    public SecureInboxService(
            CaseRepository caseRepository,
            ActivityRepository activityRepository,
            CaseEmailRepository emailRepository,
            EmailAttachmentRepository attachmentRepository,
            DatabaseGuard databaseGuard,
            JsonMapper jsonMapper) {
        this.caseRepository = caseRepository;
        this.activityRepository = activityRepository;
        this.emailRepository = emailRepository;
        this.attachmentRepository = attachmentRepository;
        this.databaseGuard = databaseGuard;
        this.jsonMapper = jsonMapper;
    }

    // ---------------------------------------------------------------- customer

    @Transactional(readOnly = true)
    public List<ConversationSummary> customerInbox(String customer, String query, CaseStatus status) {
        return databaseGuard.run(() ->
                toSummaries(caseRepository.searchOwnedCases(customer, status, likePattern(query))));
    }

    @Transactional(readOnly = true)
    public ConversationDetail customerConversation(Long caseId, String customer) {
        return databaseGuard.run(() -> toDetail(ownedCase(caseId, customer)));
    }

    /** A new message from the customer: always opens a new case. */
    @Transactional
    public ConversationDetail startConversation(
            String customer, String subject, String body, List<MultipartFile> files) {

        String cleanSubject = requireText(subject, "Subject", 200);
        String cleanBody = requireText(body, "Message", MAX_BODY_LENGTH);
        checkAttachments(files);

        return databaseGuard.run(() -> {
            Case caseEntity = new Case();
            caseEntity.setTitle(cleanSubject);
            caseEntity.setDescription(truncate(cleanBody, 2000));
            caseEntity.setStatus(CaseStatus.OPEN);
            caseEntity.setOwnerUsername(customer);
            caseEntity = caseRepository.save(caseEntity);

            addMessage(caseEntity, ActivityType.INBOUND, customer,
                    new EmailData(customer, SUPPORT_ADDRESS, cleanSubject, cleanBody, Instant.now()),
                    files);

            log.info("New conversation caseId={} customer={}", caseEntity.getId(), customer);
            return toDetail(caseEntity);
        });
    }

    /** A customer reply: attached to the existing case, which is reopened if it was resolved/closed. */
    @Transactional
    public ConversationDetail customerReply(
            Long caseId, String customer, String body, List<MultipartFile> files) {

        String cleanBody = requireText(body, "Message", MAX_BODY_LENGTH);
        checkAttachments(files);

        return databaseGuard.run(() -> {
            Case caseEntity = ownedCase(caseId, customer);

            if (caseEntity.getStatus() == CaseStatus.RESOLVED
                    || caseEntity.getStatus() == CaseStatus.CLOSED) {
                caseEntity.setStatus(CaseStatus.OPEN);
            }

            addMessage(caseEntity, ActivityType.INBOUND, customer,
                    new EmailData(customer, SUPPORT_ADDRESS, replySubject(caseEntity), cleanBody, Instant.now()),
                    files);

            log.info("Customer reply caseId={} customer={}", caseId, customer);
            return toDetail(caseEntity);
        });
    }

    @Transactional(readOnly = true)
    public EmailAttachment customerAttachment(Long attachmentId, String customer) {
        return databaseGuard.run(() -> attachmentRepository.findOwnedById(attachmentId, customer)
                .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found: " + attachmentId)));
    }

    // ---------------------------------------------------------------- agent

    @Transactional(readOnly = true)
    public List<ConversationSummary> agentInbox(String query, CaseStatus status) {
        return databaseGuard.run(() ->
                toSummaries(caseRepository.searchAllCases(status, likePattern(query))));
    }

    @Transactional(readOnly = true)
    public ConversationDetail agentConversation(Long caseId) {
        return databaseGuard.run(() -> toDetail(anyCase(caseId)));
    }

    /** An agent reply: OUTBOUND activity; the customer's pending messages become completed. */
    @Transactional
    public ConversationDetail agentReply(Long caseId, String agent, String body, List<MultipartFile> files) {

        String cleanBody = requireText(body, "Message", MAX_BODY_LENGTH);
        checkAttachments(files);

        return databaseGuard.run(() -> {
            Case caseEntity = anyCase(caseId);

            activityRepository.updateStatusByType(
                    caseId, ActivityType.INBOUND, ActivityStatus.PENDING, ActivityStatus.COMPLETED);

            if (caseEntity.getStatus() == CaseStatus.OPEN) {
                caseEntity.setStatus(CaseStatus.IN_PROGRESS);
            }

            addMessage(caseEntity, ActivityType.OUTBOUND, agent,
                    new EmailData(agent, caseEntity.getOwnerUsername(), replySubject(caseEntity), cleanBody,
                            Instant.now()),
                    files);

            log.info("Agent reply caseId={} agent={}", caseId, agent);
            return toDetail(caseEntity);
        });
    }

    @Transactional
    public ConversationDetail updateStatus(Long caseId, CaseStatus status, String agent) {
        return databaseGuard.run(() -> {
            Case caseEntity = anyCase(caseId);
            caseEntity.setStatus(status);
            caseRepository.saveAndFlush(caseEntity);

            // Resolving/closing handles whatever the customer last wrote
            if (status == CaseStatus.RESOLVED || status == CaseStatus.CLOSED) {
                activityRepository.updateStatusByType(
                        caseId, ActivityType.INBOUND, ActivityStatus.PENDING, ActivityStatus.COMPLETED);
            }
            log.info("Status change caseId={} status={} agent={}", caseId, status, agent);
            return toDetail(caseEntity);
        });
    }

    @Transactional(readOnly = true)
    public EmailAttachment agentAttachment(Long attachmentId) {
        return databaseGuard.run(() -> attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found: " + attachmentId)));
    }

    // ---------------------------------------------------------------- internals

    private Case ownedCase(Long caseId, String customer) {
        return caseRepository.findByIdAndOwnerUsername(caseId, customer)
                .orElseThrow(() -> new CaseNotFoundException("Case not found: " + caseId));
    }

    private Case anyCase(Long caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException("Case not found: " + caseId));
    }

    /** Stores one message: an activity, its case_emails row, and an email_attachments row per file. */
    private void addMessage(Case caseEntity, String type, String performedBy, EmailData data,
                            List<MultipartFile> files) {

        Activity activity = new Activity();
        activity.setActivityType(type);
        activity.setDescription(truncate(data.subject() + ": " + data.body(), 2000));
        activity.setStatus(ActivityType.INBOUND.equals(type) ? ActivityStatus.PENDING : ActivityStatus.COMPLETED);
        activity.setPerformedBy(performedBy);
        activity.setActivityAt(data.sentAt());
        caseEntity.addActivity(activity);
        activity = activityRepository.save(activity);

        CaseEmail email = new CaseEmail();
        email.setActivity(activity);
        email.setEmailData(jsonMapper.writeValueAsString(data));
        email = emailRepository.save(email);

        for (MultipartFile file : nonEmpty(files)) {
            EmailAttachment attachment = new EmailAttachment();
            attachment.setEmail(email);
            attachment.setActivity(activity);
            attachment.setFileName(cleanFileName(file.getOriginalFilename()));
            attachment.setContentType(file.getContentType());
            attachment.setSizeBytes(file.getSize());
            try {
                attachment.setAttachmentData(file.getBytes());
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read attachment", e);
            }
            attachmentRepository.save(attachment);
        }

        // Touch the case so updated_at reflects the latest message
        caseEntity.setUpdatedAt(data.sentAt());
        caseRepository.saveAndFlush(caseEntity);
    }

    private ConversationDetail toDetail(Case caseEntity) {
        Map<Long, List<AttachmentInfo>> attachmentsByEmail = attachmentRepository
                .findInfoByCaseId(caseEntity.getId())
                .stream()
                .collect(Collectors.groupingBy(AttachmentInfo::emailId));

        List<MessageResponse> messages = emailRepository.findByCaseId(caseEntity.getId())
                .stream()
                .map(email -> {
                    EmailData data = jsonMapper.readValue(email.getEmailData(), EmailData.class);
                    Activity activity = email.getActivity();
                    return new MessageResponse(
                            email.getId(),
                            activity.getId(),
                            activity.getActivityType(),
                            data.from(),
                            data.to(),
                            data.subject(),
                            data.body(),
                            data.sentAt(),
                            attachmentsByEmail.getOrDefault(email.getId(), List.of()));
                })
                .toList();

        return new ConversationDetail(
                caseEntity.getId(),
                caseEntity.getTitle(),
                caseEntity.getStatus(),
                caseEntity.getOwnerUsername(),
                caseEntity.getCreatedAt(),
                caseEntity.getUpdatedAt(),
                messages);
    }

    private List<ConversationSummary> toSummaries(List<Case> cases) {
        if (cases.isEmpty()) {
            return List.of();
        }
        Map<Long, Activity> latest = activityRepository
                .findLatestByCaseIds(cases.stream().map(Case::getId).toList())
                .stream()
                .collect(Collectors.toMap(a -> a.getCaseEntity().getId(), Function.identity()));

        return cases.stream()
                .map(c -> {
                    Activity last = latest.get(c.getId());
                    String direction = last != null ? last.getActivityType() : null;
                    return new ConversationSummary(
                            c.getId(),
                            c.getTitle(),
                            c.getStatus(),
                            c.getOwnerUsername(),
                            c.getCreatedAt(),
                            last != null ? last.getActivityAt() : c.getCreatedAt(),
                            direction,
                            last != null ? preview(last.getDescription(), c.getTitle()) : null,
                            ActivityType.INBOUND.equals(direction)
                                    && last.getStatus() == ActivityStatus.PENDING);
                })
                .sorted(Comparator.comparing(ConversationSummary::lastMessageAt).reversed())
                .toList();
    }

    /** Activity descriptions are "subject: body"; show just the body. */
    private static String preview(String description, String subject) {
        String text = description;
        for (String prefix : List.of(subject + ": ", "Re: " + subject + ": ")) {
            if (text.startsWith(prefix)) {
                text = text.substring(prefix.length());
            }
        }
        text = text.replaceAll("\\s+", " ").trim();
        return truncate(text, PREVIEW_LENGTH);
    }

    private static String replySubject(Case caseEntity) {
        return "Re: " + caseEntity.getTitle();
    }

    private static void checkAttachments(List<MultipartFile> files) {
        if (nonEmpty(files).size() > MAX_ATTACHMENTS) {
            throw new IllegalArgumentException("At most " + MAX_ATTACHMENTS + " attachments per message");
        }
    }

    private static List<MultipartFile> nonEmpty(List<MultipartFile> files) {
        return files == null ? List.of() : files.stream().filter(f -> f != null && !f.isEmpty()).toList();
    }

    /** Keeps only the base name, so a client cannot smuggle a path into file_name. */
    private static String cleanFileName(String original) {
        if (original == null || original.isBlank()) {
            return "attachment";
        }
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"]", "_").trim();
        return name.isEmpty() ? "attachment" : truncate(name, 255);
    }

    private static String requireText(String value, String field, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new IllegalArgumentException(field + " must be at most " + max + " characters");
        }
        return trimmed;
    }

    private static String likePattern(String query) {
        return query == null || query.isBlank() ? null : "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }
}
