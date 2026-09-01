package com.example.case_viewer.controller;

import com.example.case_viewer.dto.RegisterRequest;
import com.example.case_viewer.dto.RegisterResponse;
import com.example.case_viewer.service.AuthService;
import com.example.case_viewer.dto.LoginRequest;
import com.example.case_viewer.dto.LoginResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {
    
        String token = authService.login(request);
    
        return new LoginResponse(token);
    }
}