package com.example.case_viewer.exception;

public class AttachmentNotFoundException extends RuntimeException {

    public AttachmentNotFoundException(String message) {
        super(message);
    }
}
