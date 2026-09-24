package com.example.case_viewer.controller;

import com.example.case_viewer.dto.CaseResponse;
import com.example.case_viewer.dto.CaseSummaryResponse;
import com.example.case_viewer.entity.CaseStatus;
import com.example.case_viewer.service.CaseService;

import com.example.case_viewer.dto.CaseRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;
import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    // SEARCH MY CASES
    // GET /api/cases?q=printer&status=OPEN
    @GetMapping
    public List<CaseSummaryResponse> searchCases(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(required = false) CaseStatus status,
            Principal principal) {

        return caseService.searchCases(query, status, principal.getName());
    }

    // GET CASE WITH ACTIVITIES
    @GetMapping("/{caseId}")
public CaseResponse getCase(
        @PathVariable Long caseId,
       // @RequestHeader("tenant-id") String tenantId) 
       @RequestHeader(value="tenant-id", required=false) String tenantId,
       Principal principal) {

    return caseService.getCase(caseId, principal.getName());
}

     // CREATE CASE
     @PostMapping({"", "/create"})
     @ResponseStatus(HttpStatus.CREATED)
     public CaseResponse createCase(@Valid @RequestBody CaseRequest request, Principal principal) {
         return caseService.createCase(request, principal.getName());
     }
     @GetMapping("/search/jdbc/all")
     public List<com.example.case_viewer.mapper.CaseResponse> getAllCases() {
         return caseService.getAllCases();
     }

     @GetMapping("/jdbc/{caseId}")
     public com.example.case_viewer.mapper.CaseResponse getJDBCCase(@PathVariable Long caseId) {
         return caseService.getJDBCCase(caseId);
     }

     // GET CASE WITH ACTIVITIES
     @GetMapping("/search/{caseId}")
     public CaseResponse findCase(@PathVariable Long caseId, Principal principal) {
         return caseService.findCase(caseId, principal.getName());
     }


    
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
