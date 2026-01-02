package com.auth.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.auth.dto.AuthRequest;
import com.auth.dto.AuthResponse;
import com.auth.dto.RegisterRequest;
import com.auth.dto.UserProfileResponse;
import com.auth.model.User;
import com.auth.repository.UserRepository;

import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase();
        String fullName = request.getFullName().trim();
        String phone = request.getPhone().trim();
        return userRepository.findByEmail(email)
                .flatMap(existing -> Mono.<User>error(
                        new ResponseStatusException(HttpStatus.CONFLICT, "User already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = User.builder()
                            .fullName(fullName)
                            .phone(phone)
                            .email(email)
                            .password(passwordEncoder.encode(request.getPassword()))
                            .role("CITIZEN")
                            .createdAt(Instant.now())
                            .build();
                    return userRepository.save(user);
                }))
                .flatMap(this::toAuthResponse);
    }

    public Mono<AuthResponse> login(AuthRequest request) {
        String email = request.getEmail().toLowerCase();
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "Invalid credentials"));
                    }
                    return toAuthResponse(user);
                });
    }

    public Mono<UserProfileResponse> currentUser(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .map(user -> UserProfileResponse.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .build());
    }

    private Mono<AuthResponse> toAuthResponse(User user) {
        Instant expiresAt = jwtService.buildExpiry();
        return jwtService.generateToken(user, expiresAt)
                .map(token -> AuthResponse.builder()
                        .token(token)
                        .expiresAt(expiresAt)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .build());
    }
}
