package com.example.case_viewer.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log =
            LoggerFactory.getLogger(AuditLogAspect.class);

    @Around("@annotation(com.example.case_viewer.aspect.AuditLog)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName =
                joinPoint.getSignature().toShortString();

        long startTime = System.currentTimeMillis();

        log.info("START========> - {}", methodName);

        try {

            Object result = joinPoint.proceed();

            long executionTime =
                    System.currentTimeMillis() - startTime;

            log.info(
                    "END - {} | executionTime={} ms",
                    methodName,
                    executionTime
            );

            return result;

        } catch (Exception ex) {

            long executionTime =
                    System.currentTimeMillis() - startTime;

            log.error(
                    "ERROR - {} | executionTime={} ms",
                    methodName,
                    executionTime,
                    ex
            );

            throw ex;
        }
    }
}