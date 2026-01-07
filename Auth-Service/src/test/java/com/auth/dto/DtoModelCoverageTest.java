package com.auth.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.auth.model.User;

class DtoModelCoverageTest {

    @Test
    void authResponseEqualityAndAccessors() {
        Instant expiry = Instant.now();
        AuthResponse a = AuthResponse.builder()
                .token("t")
                .expiresAt(expiry)
                .userId("u1")
                .email("e1")
                .fullName("name")
                .phone("123")
                .role("ADMIN")
                .departmentId("D1")
                .build();
        AuthResponse b = AuthResponse.builder()
                .token("t")
                .expiresAt(expiry)
                .userId("u1")
                .email("e1")
                .fullName("name")
                .phone("123")
                .role("ADMIN")
                .departmentId("D1")
                .build();

        assertThat(a).isEqualTo(b);
        a.setToken("t2");
        assertThat(a.getToken()).isEqualTo("t2");
    }

    @Test
    void registerAndDepartmentRequestsCarryValues() {
        RegisterRequest register = RegisterRequest.builder()
                .fullName("Full")
                .email("email@example.com")
                .phone("1234567890")
                .password("password123")
                .departmentId("DEP1")
                .build();

        DepartmentRegisterRequest depReg = DepartmentRegisterRequest.builder()
                .fullName("Dept User")
                .email("dep@example.com")
                .phone("0987654321")
                .password("secretpass")
                .departmentId("DEP2")
                .build();

        DepartmentRequest departmentRequest = new DepartmentRequest();
        departmentRequest.setId("D1");
        departmentRequest.setName("Water");
        departmentRequest.setLevel("CENTRAL");
        departmentRequest.setCategories(List.of(new CategoryRequest()));

        assertThat(register.getDepartmentId()).isEqualTo("DEP1");
        assertThat(depReg.getEmail()).isEqualTo("dep@example.com");
        assertThat(departmentRequest.getLevel()).isEqualTo("CENTRAL");
        assertThat(departmentRequest.getCategories()).hasSize(1);
    }

    @Test
    void userProfileAndUserModelHaveDefaultsAndMutators() {
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("u1")
                .email("u1@example.com")
                .fullName("User One")
                .phone("123")
                .role("ADMIN")
                .departmentId("D9")
                .build();

        User user = User.builder()
                .email("u1@example.com")
                .fullName("User One")
                .build();

        assertThat(profile.getDepartmentId()).isEqualTo("D9");
        assertThat(user.getRole()).isEqualTo(com.auth.model.UserRole.CITIZEN.value());

        user.setRole("ADMIN");
        user.setDepartmentId("DEP");
        assertThat(user.getDepartmentId()).isEqualTo("DEP");
        assertThat(user.getRole()).isEqualTo("ADMIN");
    }
}
