package com.example.case_viewer.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class LoggingAspect {


    private static final Logger log =
    LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.example.case_viewer.service..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint)
            throws Throwable {

        long start = System.currentTimeMillis();

        String methodName =
                joinPoint.getSignature().toShortString();

        log.info("Entering========> {}", methodName);

        try {
            Object result = joinPoint.proceed();

            long executionTime =
                    System.currentTimeMillis() - start;

            log.info("Completed========> {} in {} ms",
                    methodName, executionTime);

            return result;

        } catch (Exception ex) {

            long executionTime =
                    System.currentTimeMillis() - start;

            log.error("Failed {} after {} ms",
                    methodName, executionTime, ex);

            throw ex;
        }
    }
}