package com.example.case_viewer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.time.Instant;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(CaseNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleCaseNotFound(
                        CaseNotFoundException ex) {

                ErrorResponse error = new ErrorResponse(
                                "CASE_NOT_FOUND",
                                ex.getMessage(),
                                Instant.now());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(error);
        }

        @ExceptionHandler(AttachmentNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleAttachmentNotFound(
                        AttachmentNotFoundException ex) {

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(new ErrorResponse("ATTACHMENT_NOT_FOUND", ex.getMessage(), Instant.now()));
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponse> handleUploadTooLarge(
                        MaxUploadSizeExceededException ex) {

                return ResponseEntity
                                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .body(new ErrorResponse("ATTACHMENT_TOO_LARGE",
                                                "Attachments are too large (max 10 MB each, 25 MB per message)",
                                                Instant.now()));
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingParameter(
                        MissingServletRequestParameterException ex) {

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(new ErrorResponse("VALIDATION_ERROR",
                                                ex.getParameterName() + " is required", Instant.now()));
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