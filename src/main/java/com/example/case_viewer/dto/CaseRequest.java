package com.example.case_viewer.dto;

import com.example.case_viewer.entity.CaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The case number is not supplied: it is the generated case id (1000, 1001, ...).
 * status defaults to OPEN when omitted.
 */
public record CaseRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        CaseStatus status
) {
}
