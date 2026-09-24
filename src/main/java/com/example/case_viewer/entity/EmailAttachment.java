package com.example.case_viewer.entity;

import jakarta.persistence.*;

/**
 * A file attached to a message. The bytes live in attachment_data; list views read
 * only the metadata columns (see EmailAttachmentRepository) so blobs are loaded on download only.
 */
@Entity
@Table(
        name = "email_attachments",
        indexes = {
                @Index(name = "idx_email_attachments_email_id", columnList = "email_id"),
                @Index(name = "idx_email_attachments_activity_id", columnList = "activity_id")
        }
)
public class EmailAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_id_seq")
    @SequenceGenerator(name = "attachment_id_seq", sequenceName = "attachment_id_seq", initialValue = 1000, allocationSize = 1)
    @Column(name = "attachment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "email_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_email_attachments_email")
    )
    private CaseEmail email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "activity_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_email_attachments_activity")
    )
    private Activity activity;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Lob
    @Column(name = "attachment_data", nullable = false)
    private byte[] attachmentData;

    public EmailAttachment() {
    }

    public Long getId() {
        return id;
    }

    public CaseEmail getEmail() {
        return email;
    }

    public Activity getActivity() {
        return activity;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public byte[] getAttachmentData() {
        return attachmentData;
    }

    public void setEmail(CaseEmail email) {
        this.email = email;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public void setAttachmentData(byte[] attachmentData) {
        this.attachmentData = attachmentData;
    }
}
