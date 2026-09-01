package com.example.case_viewer.exception;

public class DatabaseCapacityException extends RuntimeException {

    public DatabaseCapacityException(String message) {
        super(message);
    }
}