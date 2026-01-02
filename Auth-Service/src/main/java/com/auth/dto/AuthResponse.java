package com.auth.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String token;
    Instant expiresAt;
    String userId;
    String email;
    String fullName;
    String phone;
    String role;
}
