package com.example.case_viewer.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "cases",
        indexes = {
                @Index(name = "idx_cases_status", columnList = "status"),
                @Index(name = "idx_cases_owner_username", columnList = "owner_username")
        }
)
public class Case {

    // The case's only identifier: sequential, starting at 1000
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_id_seq")
    @SequenceGenerator(name = "case_id_seq", sequenceName = "case_id_seq", initialValue = 1000, allocationSize = 1)
    @Column(name = "case_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CaseStatus status;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "owner_username", length = 100)
    private String ownerUsername;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @OneToMany(
            mappedBy = "caseEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("activityAt ASC")
    private List<Activity> activities = new ArrayList<>();

    public Case() {
    }

    public Long getId() {
        return id;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setStatus(CaseStatus status) {
        this.status = status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
        activity.setCaseEntity(this);
    }

    public void removeActivity(Activity activity) {
        activities.remove(activity);
        activity.setCaseEntity(null);
    }
}
