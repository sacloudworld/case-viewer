package com.example.case_viewer.mapper;


import java.time.LocalDateTime;

public class CaseResponse {

    private Long caseId;
    private String status;
    private LocalDateTime updatedAt;

    public CaseResponse() {
    }

    public CaseResponse(
                        Long caseId,
                        String status,
                        LocalDateTime updatedAt) {

        this.caseId = caseId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
