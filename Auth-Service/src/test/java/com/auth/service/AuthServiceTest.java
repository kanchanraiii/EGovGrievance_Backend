package com.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.auth.dto.AuthRequest;
import com.auth.dto.DepartmentRegisterRequest;
import com.auth.dto.RegisterRequest;
import com.auth.model.User;
import com.auth.model.UserRole;
import com.auth.repository.UserRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private DepartmentCatalog departmentCatalog;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private Instant fixedExpiry;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("USER@example.com")
                .phone("1234567890")
                .password("password123")
                .departmentId("dept-1")
                .build();
        fixedExpiry = Instant.parse("2026-01-01T00:00:00Z");

        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pass");
        when(jwtService.buildExpiry()).thenReturn(fixedExpiry);
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.just("jwt-token"));
        when(departmentCatalog.isValid(anyString())).thenReturn(true);
    }

    @Test
    void registerCitizenCreatesUserAndReturnsToken() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.empty());
        doAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId("generated-id");
            return Mono.just(toSave);
        }).when(userRepository).save(any(User.class));

        StepVerifier.create(authService.registerCitizen(registerRequest))
                .assertNext(response -> {
                    assertThat(response.getToken()).isEqualTo("jwt-token");
                    assertThat(response.getUserId()).isEqualTo("generated-id");
                    assertThat(response.getEmail()).isEqualTo("user@example.com");
                    assertThat(response.getRole()).isEqualTo(UserRole.CITIZEN.value());
                })
                .verifyComplete();

        verify(userRepository).findByEmail("user@example.com");
        verify(jwtService).generateToken(any(User.class), any());
    }

    @Test
    void registerCaseWorkerRequiresDepartmentId() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("case@example.com")
                .phone("9876543210")
                .password("secret123")
                .departmentId(null)
                .build();

        StepVerifier.create(authService.registerCaseWorker(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verify();

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerDepartmentOfficerValidatesDepartmentAndUniqueness() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());
        when(departmentCatalog.isValid("DEPT-1")).thenReturn(true);
        when(userRepository.findByDepartmentIdAndRole("DEPT-1", UserRole.DEPARTMENT_OFFICER.value()))
                .thenReturn(Mono.just(new User()));

        DepartmentRegisterRequest request = new DepartmentRegisterRequest();
        request.setFullName("Officer One");
        request.setEmail("officer@example.com");
        request.setPhone("1112223333");
        request.setPassword("password123");
        request.setDepartmentId("dept-1");

        StepVerifier.create(authService.registerDepartmentOfficer(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
    }

    @Test
    void registerDepartmentOfficerRejectsUnknownDepartment() {
        when(departmentCatalog.isValid(anyString())).thenReturn(false);

        DepartmentRegisterRequest request = new DepartmentRegisterRequest();
        request.setFullName("Officer Two");
        request.setEmail("officer2@example.com");
        request.setPhone("1112223333");
        request.setPassword("password123");
        request.setDepartmentId("missing");

        StepVerifier.create(authService.registerDepartmentOfficer(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                })
                .verify();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.just(new User()));

        StepVerifier.create(authService.registerCitizen(registerRequest))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
    }

    @Test
    void loginRejectsBadPassword() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("hashed");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        StepVerifier.create(authService.login(new AuthRequest("user@example.com", "wrong")))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                })
                .verify();
    }

    @Test
    void loginRoleMismatchIsForbidden() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("hash");
        user.setRole(UserRole.CITIZEN.value());

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.just("jwt"));

        StepVerifier.create(authService.loginAdmin(new AuthRequest("admin@example.com", "pwd")))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                })
                .verify();
    }

    @Test
    void loginPropagatesJwtFailures() {
        User user = new User();
        user.setId("user-id");
        user.setEmail("user@example.com");
        user.setPassword("hash");
        user.setFullName("User");
        user.setPhone("12345");
        user.setRole(UserRole.CITIZEN.value());

        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.error(new IllegalArgumentException("jwt error")));

        StepVerifier.create(authService.login(new AuthRequest("user@example.com", "pwd")))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verify();
    }

    @Test
    void currentUserReturnsProfile() {
        User user = new User();
        user.setId("user-id");
        user.setEmail("user@example.com");
        user.setFullName("User");
        user.setPhone("12345");
        user.setRole(UserRole.ADMIN.value());
        user.setDepartmentId("D1");

        when(userRepository.findById("user-id")).thenReturn(Mono.just(user));

        StepVerifier.create(authService.currentUser("user-id"))
                .assertNext(profile -> {
                    assertThat(profile.getUserId()).isEqualTo("user-id");
                    assertThat(profile.getEmail()).isEqualTo("user@example.com");
                    assertThat(profile.getDepartmentId()).isEqualTo("D1");
                })
                .verifyComplete();
    }

    @Test
    void currentUserNotFound() {
        when(userRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(authService.currentUser("missing"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                })
                .verify();
    }

    @Test
    void registerRejectsMissingRequiredFields() {
        RegisterRequest invalid = new RegisterRequest();

        StepVerifier.create(authService.registerCitizen(invalid))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verify();
    }
}
