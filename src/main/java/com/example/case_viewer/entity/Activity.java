package com.example.case_viewer.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "case_activities",
        indexes = {
                @Index(name = "idx_case_activities_case_id", columnList = "case_id"),
                @Index(name = "idx_case_activities_activity_at", columnList = "activity_at")
        }
)
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "case_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_case_activities_case")
    )
    private Case caseEntity;

    @Column(name = "activity_type", nullable = false, length = 100)
    private String activityType;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActivityStatus status;

    @Column(name = "performed_by", length = 100)
    private String performedBy;

    @Column(name = "activity_at", nullable = false)
    private Instant activityAt;

    public Activity() {
    }

    public UUID getId() {
        return id;
    }

    public Case getCaseEntity() {
        return caseEntity;
    }

    public String getActivityType() {
        return activityType;
    }

    public String getDescription() {
        return description;
    }

    public ActivityStatus getStatus() {
        return status;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public Instant getActivityAt() {
        return activityAt;
    }

    public void setCaseEntity(Case caseEntity) {
        this.caseEntity = caseEntity;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public void setActivityAt(Instant activityAt) {
        this.activityAt = activityAt;
    }
}
