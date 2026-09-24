package com.example.case_viewer.entity;

import jakarta.persistence.*;

/**
 * The message behind an INBOUND/OUTBOUND activity. email_data holds the message
 * as JSON (from, to, subject, body, sentAt).
 */
@Entity
@Table(
        name = "case_emails",
        indexes = @Index(name = "idx_case_emails_activity_id", columnList = "activity_id")
)
public class CaseEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_id_seq")
    @SequenceGenerator(name = "email_id_seq", sequenceName = "email_id_seq", initialValue = 1000, allocationSize = 1)
    @Column(name = "email_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "activity_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_case_emails_activity")
    )
    private Activity activity;

    @Lob
    @Column(name = "email_data", nullable = false)
    private String emailData;

    public CaseEmail() {
    }

    public Long getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public String getEmailData() {
        return emailData;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setEmailData(String emailData) {
        this.emailData = emailData;
    }
}
