package com.example.case_viewer.controller;

import com.example.case_viewer.dto.CurrentUserResponse;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Who is signed in, so each UI can check the role before calling role-restricted APIs. */
@RestController
public class MeController {

    @GetMapping("/api/me")
    public CurrentUserResponse me(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .findFirst()
                .orElse("USER");
        return new CurrentUserResponse(authentication.getName(), role);
    }
}
