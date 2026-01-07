package com.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Registration payload for roles that require a department assignment
 * (CASE_WORKER, DEPARTMENT_OFFICER).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRegisterRequest {

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

    @NotBlank(message = "departmentId is required")
    private String departmentId;
}
