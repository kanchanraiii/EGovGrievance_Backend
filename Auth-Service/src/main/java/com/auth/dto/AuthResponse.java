package com.auth.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Instant expiresAt;
    private String userId;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private String departmentId;
}
