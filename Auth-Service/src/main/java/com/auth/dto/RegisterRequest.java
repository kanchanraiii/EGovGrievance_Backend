package com.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9+\\-()\\s]{7,15}$", message = "Enter a valid phone number")
    private String phone;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    // Optional for SO, Admin, Citizen required for case-worker and department-officer endpoints
    private String departmentId;
}
