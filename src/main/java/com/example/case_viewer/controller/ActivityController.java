package com.example.case_viewer.controller;

import com.example.case_viewer.dto.ActivityRequest;
import com.example.case_viewer.dto.CaseResponse;
import com.example.case_viewer.service.CaseService;

import com.example.case_viewer.dto.CaseRequest;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    /* 

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

 

     // GET CASE WITH ACTIVITIES
     @PostMapping("/create")
     public CaseResponse createActvity(@RequestBody ActivityRequest request) {
         return activityService.createActvity(request);
     }
*/
    /*
    {
    "caseNumber": "CASE-1001",
    "status": "OPEN",
    "customerId": 123
    }
        @PostMapping("/search")
    public List<CaseResponse> searchCases(
            @RequestBody CaseSearchRequest request) {

        return caseService.searchCases(request);
    }

    GET /api/cases?status=OPEN&priority=HIGH&customerId=1001
    @GetMapping
    public List<CaseResponse> searchCases(
            @RequestParam String status,
            @RequestParam String priority,
            @RequestParam Long customerId) {

        return caseService.searchCases(
                status,
                priority,
                customerId
        );
    }

    PostMapping
    public CaseResponse createCase(
            @RequestBody CreateCaseRequest request) {
        // Create case
    }

    @PutMapping("/{caseNumber}")
    public CaseResponse updateCase(
            @PathVariable String caseNumber,
            @RequestBody UpdateCaseRequest request) {
        // Replace/update case
    }

    @PatchMapping("/{caseNumber}")
    public CaseResponse updateCasePartially(
            @PathVariable String caseNumber,
            @RequestBody UpdateCaseRequest request) {
        // Partial update
    }

    @DeleteMapping("/{caseNumber}")
    public void deleteCase(
            @PathVariable String caseNumber) {
        // Delete case
    }
    
    */

}
