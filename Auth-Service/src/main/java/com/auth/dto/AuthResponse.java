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
    private String departmentId;

    public AuthResponse() {
        // default constructor for serializers
    }

    public AuthResponse(String token, Instant expiresAt, String userId, String email, String fullName, String phone, String role, String departmentId) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
        this.departmentId = departmentId;
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

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    // Manual builder to satisfy code that expects AuthResponse.builder()
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private Instant expiresAt;
        private String userId;
        private String email;
        private String fullName;
        private String phone;
        private String role;
        private String departmentId;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder departmentId(String departmentId) {
            this.departmentId = departmentId;
            return this;
        }

        public AuthResponse build() {
            return new AuthResponse(token, expiresAt, userId, email, fullName, phone, role, departmentId);
        }
    }
}
