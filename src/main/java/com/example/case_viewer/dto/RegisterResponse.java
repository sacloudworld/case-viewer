package com.example.case_viewer.dto;

public record RegisterResponse(
        Long id,
        String username,
        String role
) {
}