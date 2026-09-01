package com.example.case_viewer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.time.Instant;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(CaseNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleCaseNotFound(
                        CaseNotFoundException ex) {

                ErrorResponse error = new ErrorResponse(
                                "ORDER_NOT_FOUND",
                                ex.getMessage(),
                                Instant.now());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(
                        MethodArgumentNotValidException ex) {

                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(error -> error.getDefaultMessage())
                                .orElse("Invalid request");

                ErrorResponse error = new ErrorResponse(
                                "VALIDATION_ERROR",
                                message,
                                Instant.now());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        IllegalArgumentException ex) {

                ErrorResponse error = new ErrorResponse(
                                "INVALID_ARGUMENT",
                                ex.getMessage(),
                                Instant.now());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

       

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleInvalidRequest(
                HttpMessageNotReadableException ex) {
        
            ErrorResponse error = new ErrorResponse(
                    "INVALID_ARGUMENT",
                    "Invalid request body",
                    Instant.now()
            );
        
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
        }

        @ExceptionHandler(DatabaseCapacityException.class)
                public ResponseEntity<String> handleDatabaseCapacity(
                        DatabaseCapacityException ex) {

                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ex.getMessage());
                }
}