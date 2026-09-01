package com.example.case_viewer.dto;

import com.example.case_viewer.entity.CaseStatus;
public record CaseRequest(
        String caseNumber,
        String title,
        String description,
        CaseStatus status
       

) {
 
    
}
