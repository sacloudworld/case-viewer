package com.example.case_viewer.service;

import com.example.case_viewer.dto.RegisterRequest;
import com.example.case_viewer.dto.RegisterResponse;
import com.example.case_viewer.entity.User;
import com.example.case_viewer.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.case_viewer.dto.LoginRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final UserCacheService userCacheService;

        public AuthService(
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuthenticationManager authenticationManager,
                        JwtService jwtService,
                        UserCacheService userCacheService) {

                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.authenticationManager = authenticationManager;
                this.jwtService = jwtService;
                this.userCacheService = userCacheService;
        }

        public String login(LoginRequest request) {

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.username(),
                                                request.password()));

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                return jwtService.generateToken(userDetails);
        }

        public RegisterResponse register(RegisterRequest request) {

                if (userRepository.findByUsername(request.username()).isPresent()) {
                        throw new IllegalArgumentException(
                                        "Username already exists");
                }

                User user = new User();

                user.setUsername(request.username());

                user.setPassword(
                                passwordEncoder.encode(request.password()));

                user.setRole("USER");
                user.setEnabled(true);

                User savedUser = userRepository.save(user);

                userCacheService.cacheUser(savedUser);

                return new RegisterResponse(
                                savedUser.getId(),
                                savedUser.getUsername(),
                                savedUser.getRole());
        }
}