package com.example.case_viewer.repository;

import com.example.case_viewer.dto.AttachmentInfo;
import com.example.case_viewer.entity.EmailAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmailAttachmentRepository extends JpaRepository<EmailAttachment, Long> {

    // Metadata only, so listing a conversation never loads attachment_data blobs
    @Query("""
            SELECT new com.example.case_viewer.dto.AttachmentInfo(
                t.id, t.email.id, t.activity.id, t.fileName, t.contentType, t.sizeBytes)
            FROM EmailAttachment t
            WHERE t.activity.caseEntity.id = :caseId
            ORDER BY t.id
            """)
    List<AttachmentInfo> findInfoByCaseId(@Param("caseId") Long caseId);

    @Query("""
            SELECT t FROM EmailAttachment t
            WHERE t.id = :attachmentId
              AND t.activity.caseEntity.ownerUsername = :owner
            """)
    Optional<EmailAttachment> findOwnedById(
            @Param("attachmentId") Long attachmentId,
            @Param("owner") String owner);
}
