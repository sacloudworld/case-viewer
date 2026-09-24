package com.example.case_viewer.controller;

import com.example.case_viewer.dto.ActivityRequest;
import com.example.case_viewer.dto.ActivityResponse;
import com.example.case_viewer.entity.ActivityStatus;
import com.example.case_viewer.service.ActivityService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // SEARCH ACTIVITIES OF A CASE I OWN
    // GET /api/cases/1000/activities?q=call&status=PENDING
    @GetMapping
    public List<ActivityResponse> searchActivities(
            @PathVariable Long caseId,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(required = false) ActivityStatus status,
            Principal principal) {

        return activityService.searchActivities(caseId, query, status, principal.getName());
    }

    // CREATE ACTIVITY UNDER A CASE I OWN
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityResponse createActivity(
            @PathVariable Long caseId,
            @Valid @RequestBody ActivityRequest request,
            Principal principal) {

        return activityService.createActivity(caseId, request, principal.getName());
    }
}
