package com.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.jwt.Jwt;

import com.auth.dto.AuthRequest;
import com.auth.dto.AuthResponse;
import com.auth.dto.DepartmentRegisterRequest;
import com.auth.dto.RegisterRequest;
import com.auth.dto.UserProfileResponse;
import com.auth.service.AuthService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private AuthResponse authResponse;
    private RegisterRequest registerRequest;
    private DepartmentRegisterRequest departmentRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        authResponse = AuthResponse.builder()
                .token("token-1")
                .expiresAt(Instant.parse("2026-01-01T00:00:00Z"))
                .userId("user-1")
                .email("user@example.com")
                .fullName("User Example")
                .phone("1234567890")
                .role("ROLE")
                .departmentId("DEPT-1")
                .build();

        registerRequest = RegisterRequest.builder()
                .fullName("Full Name")
                .email("user@example.com")
                .phone("1234567890")
                .password("password123")
                .departmentId("DEPT-1")
                .build();

        departmentRequest = new DepartmentRegisterRequest();
        departmentRequest.setFullName("Dept User");
        departmentRequest.setEmail("dept@example.com");
        departmentRequest.setPhone("0987654321");
        departmentRequest.setPassword("secret123");
        departmentRequest.setDepartmentId("DEPT-2");

        authRequest = new AuthRequest("user@example.com", "password123");
    }

    @Test
    void registerDelegatesToService() {
        when(authService.register(registerRequest)).thenReturn(Mono.empty());

        StepVerifier.create(authController.register(registerRequest))
                .verifyComplete();

        verify(authService).register(registerRequest);
    }

    @Test
    void registerCitizenDelegatesToService() {
        when(authService.registerCitizen(registerRequest)).thenReturn(Mono.empty());

        StepVerifier.create(authController.registerCitizen(registerRequest))
                .verifyComplete();

        verify(authService).registerCitizen(registerRequest);
    }

    @Test
    void registerCaseWorkerDelegatesToService() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("departmentId")).thenReturn("DEPT-1");
        when(authService.registerCaseWorkerForDepartment(registerRequest, "DEPT-1")).thenReturn(Mono.empty());

        StepVerifier.create(authController.registerCaseWorker(jwt, registerRequest))
                .verifyComplete();

        verify(authService).registerCaseWorkerForDepartment(registerRequest, "DEPT-1");
    }

    @Test
    void registerAdminDelegatesToService() {
        when(authService.registerAdmin(registerRequest)).thenReturn(Mono.empty());

        StepVerifier.create(authController.registerAdmin(registerRequest))
                .verifyComplete();

        verify(authService).registerAdmin(registerRequest);
    }

    @Test
    void registerSupervisoryOfficerDelegatesToService() {
        when(authService.registerSupervisoryOfficer(registerRequest)).thenReturn(Mono.empty());

        StepVerifier.create(authController.registerSupervisoryOfficer(registerRequest))
                .verifyComplete();

        verify(authService).registerSupervisoryOfficer(registerRequest);
    }

    @Test
    void registerDepartmentOfficerDelegatesToService() {
        when(authService.registerDepartmentOfficer(departmentRequest)).thenReturn(Mono.empty());

        StepVerifier.create(authController.registerDepartmentOfficer(departmentRequest))
                .verifyComplete();

        verify(authService).registerDepartmentOfficer(departmentRequest);
    }

    @Test
    void loginDelegatesToService() {
        when(authService.login(authRequest)).thenReturn(Mono.just(authResponse));

        StepVerifier.create(authController.login(authRequest))
                .expectNext(authResponse)
                .verifyComplete();

        verify(authService).login(authRequest);
    }

    @Test
    void loginCitizenDelegatesToService() {
        when(authService.loginCitizen(authRequest)).thenReturn(Mono.just(authResponse));

        StepVerifier.create(authController.loginCitizen(authRequest))
                .expectNext(authResponse)
                .verifyComplete();

        verify(authService).loginCitizen(authRequest);
    }

    @Test
    void loginCaseWorkerDelegatesToService() {
        when(authService.loginCaseWorker(authRequest)).thenReturn(Mono.just(authResponse));

        StepVerifier.create(authController.loginCaseWorker(authRequest))
                .expectNext(authResponse)
                .verifyComplete();

        verify(authService).loginCaseWorker(authRequest);
    }

    @Test
    void loginAdminDelegatesToService() {
        when(authService.loginAdmin(authRequest)).thenReturn(Mono.just(authResponse));

        StepVerifier.create(authController.loginAdmin(authRequest))
                .expectNext(authResponse)
                .verifyComplete();

        verify(authService).loginAdmin(authRequest);
    }

    @Test
    void loginSupervisoryOfficerDelegatesToService() {
        when(authService.loginSupervisoryOfficer(authRequest)).thenReturn(Mono.just(authResponse));

        StepVerifier.create(authController.loginSupervisoryOfficer(authRequest))
                .expectNext(authResponse)
                .verifyComplete();

        verify(authService).loginSupervisoryOfficer(authRequest);
    }

    @Test
    void loginDepartmentOfficerDelegatesToService() {
        when(authService.loginDepartmentOfficer(authRequest)).thenReturn(Mono.just(authResponse));

        StepVerifier.create(authController.loginDepartmentOfficer(authRequest))
                .expectNext(authResponse)
                .verifyComplete();

        verify(authService).loginDepartmentOfficer(authRequest);
    }

    @Test
    void meReturnsCurrentUserProfile() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("user-1");
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("user-1")
                .email("user@example.com")
                .fullName("User Example")
                .phone("1234567890")
                .role("ROLE")
                .departmentId("DEPT-1")
                .build();
        when(authService.currentUser("user-1")).thenReturn(Mono.just(profile));

        StepVerifier.create(authController.me(jwt))
                .assertNext(response -> assertThat(response.getUserId()).isEqualTo("user-1"))
                .verifyComplete();

        verify(authService).currentUser("user-1");
    }
}
