package com.example.case_viewer.dto;

import com.example.case_viewer.mybatis.entity.*;
import java.util.UUID;

public class ActivityRequest {
    private String activityType;
    private UUID caseId;
    private String description;
    private String performedBy;
}
