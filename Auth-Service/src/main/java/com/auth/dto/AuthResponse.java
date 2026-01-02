package com.auth.dto;

import java.time.Instant;

public class AuthResponse {
    private String token;
    private Instant expiresAt;
    private String userId;
    private String email;
    private String fullName;
    private String phone;
    private String role;

    public AuthResponse() {
        // default constructor for serializers
    }

    public AuthResponse(String token, Instant expiresAt, String userId, String email, String fullName, String phone, String role) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
