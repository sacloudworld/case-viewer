package com.example.case_viewer.service;

import com.example.case_viewer.dto.ActivityResponse;
import com.example.case_viewer.dto.CaseResponse;
import com.example.case_viewer.entity.Activity;
import com.example.case_viewer.entity.Case;
import com.example.case_viewer.exception.CaseNotFoundException;
import com.example.case_viewer.exception.DatabaseCapacityException;
import com.example.case_viewer.repository.CaseRepository;

import com.example.case_viewer.dto.CaseRequest;
import com.example.case_viewer.mapper.CaseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.Semaphore;

@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    private final CaseRepository caseRepository;
    private final Semaphore databaseSemaphore;
    private final CaseMapper caseMapper;

    public CaseService(CaseRepository caseRepository, Semaphore databaseSemaphore,CaseMapper caseMapper) {
        this.caseRepository = caseRepository;
        this.databaseSemaphore = databaseSemaphore;
        this.caseMapper = caseMapper;
    }

    @Transactional(readOnly = true)
    public CaseResponse getCase(String caseNumber) {

        log.info("Fetching case with caseNumber={}", caseNumber);

        boolean acquired = databaseSemaphore.tryAcquire();

        if (!acquired) {
            log.warn("DB SEMAPHORE REJECTED - no permit available");
            throw new DatabaseCapacityException("Database capacity exceeded");
        }

        try {
            Case caseEntity = caseRepository.findByCaseNumber(caseNumber)
                    .orElseThrow(() -> new CaseNotFoundException(
                            "Case not found: " + caseNumber));

            List<ActivityResponse> activities = caseEntity.getActivities()
                    .stream()
                    .map(this::toActivityResponse)
                    .toList();

            return new CaseResponse(
                    caseEntity.getId(),
                    caseEntity.getCaseNumber(),
                    caseEntity.getTitle(),
                    caseEntity.getDescription(),
                    caseEntity.getStatus(),
                    activities,
                    caseEntity.getCreatedAt(),
                    caseEntity.getUpdatedAt()
            );
        } finally {
            databaseSemaphore.release();
        }
    }


    @Transactional
    public CaseResponse createCase(CaseRequest request) {

        log.info("Create case with caseNumber={}", request);

        boolean acquired = databaseSemaphore.tryAcquire();

        if (!acquired) {
            log.warn("DB SEMAPHORE REJECTED - no permit available");
            throw new DatabaseCapacityException("Database capacity exceeded");
        }

        try {

            Case caseInfo = new Case();

            caseInfo.setCaseNumber(request.caseNumber());
            caseInfo.setTitle(request.title());
            caseInfo.setDescription(request.description());
            caseInfo.setStatus(request.status());
            
            Case caseEntity = caseRepository.saveAndFlush(caseInfo);
                    
           

            return new CaseResponse(
                    caseEntity.getId(),
                    caseEntity.getCaseNumber(),
                    caseEntity.getTitle(),
                    caseEntity.getDescription(),
                    caseEntity.getStatus(),
                    List.of(),
                    caseEntity.getCreatedAt(),
                    caseEntity.getUpdatedAt()
            );
        } finally {
            databaseSemaphore.release();
        }
    }


    public CaseResponse findCase(
        String caseNumber) {

            com.example.case_viewer.mybatis.entity.Case caseEntity = caseMapper.findCase(
                caseNumber);


            if (caseEntity == null) {
                return null;
            }
        
           // CaseResponse response = new CaseResponse();

            return new CaseResponse(
                caseEntity.getCaseId(),
                caseEntity.getCaseNumber(),
                caseEntity.getTitle(),
                caseEntity.getDescription(),
                caseEntity.getStatus(),
                List.of(),
                caseEntity.getCreatedAt(),
                caseEntity.getUpdatedAt()
        );
           
           
               

    
    
}

    private ActivityResponse toActivityResponse(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getActivityType(),
                activity.getDescription(),
                activity.getStatus(),
                activity.getPerformedBy(),
                activity.getActivityAt()
        );
    }
}
